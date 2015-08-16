package com.example.leroy.popularmovies_tallleroy;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.GridView;

import com.example.leroy.popularmovies_tallleroy.data.PostersContract;
import com.example.leroy.popularmovies_tallleroy.sync.SyncWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by LeRoy on 8/1/2015.
 *
 * complex class to do most of the data handling, instances kept as a list for the PostersFragment,
 * passed as an instance to the MovieDetail Fragment
 *
 * backed by the content provider
 *
 * will source its own poster image from a memory or disk cache, if the poster image is not present in either
 * it will fetch it from themoviedb
 *
 * will source its own runtime, trailers and reviews from themoviedb or the content provider
 * when constructed from a Cursor
 *
 */
public class MovieSummary implements Parcelable {
    public static final String LOG_TAG = MovieSummary.class.getSimpleName();
    public static String INVALID_ID = "-1";
    public static String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static Context s_context;

    public static void setActivity(Activity activity) {
        // run this once at the first time the class it used
        // determine the screen size and update the IMAGE_BASE_URL
        // to keep the onscreen size right
        s_context = activity.getApplicationContext();

        if( IMAGE_BASE_URL.endsWith("/")) {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

            int density = dm.densityDpi;
            // use a smaller bitmap for the low memory in the emulators
            if (!MainActivity.RUN_IN_EMULATOR) {
                if (density <= DisplayMetrics.DENSITY_LOW) {
                    IMAGE_BASE_URL += "w92";
                } else if (density <= DisplayMetrics.DENSITY_MEDIUM) {
                    IMAGE_BASE_URL += "w154";
                } else if (density <= DisplayMetrics.DENSITY_HIGH) {
                    IMAGE_BASE_URL += "w185";
                } else if (density <= DisplayMetrics.DENSITY_XHIGH) {
                    IMAGE_BASE_URL += "w342";
                } else if (density <= DisplayMetrics.DENSITY_XXHIGH) {
                    IMAGE_BASE_URL += "w500";
                } else {
                    // higher density devices than XXHIGH(480)
                    IMAGE_BASE_URL += "w780";
                }
            } else {
                IMAGE_BASE_URL += "w185";
            }
        }
    }

    String adult;
    String backdrop_path;
    boolean favorite = false;
    String genre_ids;
    String movie_id = INVALID_ID;
    String original_language;
    String original_title;
    String overview;
    String release_date;
    String runtime;
    String poster_path;
    String popularity;
    List<Review> reviews;
    String title;
    List<Trailer> trailers;
    String video;
    String vote_average;
    String vote_count;
    boolean bLocalBackdropBitmap = false;
    boolean bLocalPosterBitmap = false;

    private static Class<MovieSummary> myClass = MovieSummary.class;

    // make sure all of the field names are in these 'namesXXX' arrays by type
    private static String [] namesString = {"adult", "backdrop_path", "genre_ids", "original_language", "original_title",
            "overview", "release_date", "poster_path", "popularity", "title", "video", "vote_average", "vote_count", "movie_id"};
    private static String [] namesDetails = {"runtime"};
    private static String [] namesBoolean = {"bLocalBackdropBitmap", "bLocalPosterBitmap", "favorite"};
    private static String [] namesList = {"trailers", "reviews"};

    public MovieSummary(JSONObject movie) {

        try {
            // there are no bitmaps in the JSON from themoviedb
            for(String nameString : namesString) {
                if (nameString.equals("movie_id")) {
                    // special case
                    // the JSON from themoviedb uses plain old id for our movie_id
                    myClass.getDeclaredField(nameString).set(this, movie.getString("id"));
                } else {
                    myClass.getDeclaredField(nameString).set(this, movie.getString(nameString));
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected MovieSummary(Parcel in) {

        try {
            for(String nameString : namesString) {
               myClass.getDeclaredField(nameString).set(this, in.readString());
            }
            for(String nameString : namesDetails) {
                myClass.getDeclaredField(nameString).set(this, in.readString());
            }
            for(String nameString : namesBoolean) {
                myClass.getDeclaredField(nameString).set(this, in.readInt() != 0);
            }
            for(String nameString : namesList) {
                myClass.getDeclaredField(nameString).set(this, in.readArrayList(MovieSummary.Trailer.class.getClassLoader()));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MovieSummary(Cursor cursor) {
        try {
            for(String nameString : namesString) {
                int index = cursor.getColumnIndex(nameString);
                if (index >= 0) {
                    myClass.getDeclaredField(nameString).set(this, cursor.getString(index));
                }
            }
            for(String nameString : namesDetails) {
                int index = cursor.getColumnIndex(nameString);
                if (index >= 0) {
                    myClass.getDeclaredField(nameString).set(this, cursor.getString(index));
                }
            }
            for(String nameString : namesBoolean) {
                int index = cursor.getColumnIndex(nameString);
                if (index >= 0) {
                    myClass.getDeclaredField(nameString).set(this, cursor.getInt(index) != 0);
                }
            }

            // we are created from a SQL call, so fill up the details using a background task
            if (getRuntime() == null) {
                // we need to get the details from themoviedb and put them in the content provider
                new FillInMovieDetails(s_context).execute(this);
            } else {
                // the trailers and reviews are already stocked in the content provider
                queryTrailersAndReviews();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void queryTrailersAndReviews() {
        // we believe that the content resolver has been updated with trailers and reviews if any
        String [] argMovieId = { getMovieId() };
        Cursor cursor = s_context.getContentResolver().query(PostersContract.TrailersEntry.CONTENT_URI,null,"movie_id = ? ", argMovieId, null);
        if (cursor.getCount() > 0) {
            List<Trailer> listTrailers = new ArrayList<Trailer>(cursor.getCount());
            while(cursor.moveToNext()) {
                listTrailers.add(new Trailer(cursor));
            }
            trailers = listTrailers;
        }
        cursor = s_context.getContentResolver().query(PostersContract.ReviewsEntry.CONTENT_URI,null,"movie_id = ? ", argMovieId, null);
        if (cursor.getCount() > 0) {
            List<Review> listReviews = new ArrayList<Review>(cursor.getCount());
            while(cursor.moveToNext()) {
                listReviews.add(new Review(cursor));
            }
            reviews = listReviews;
        }
    }

    public static final Creator<MovieSummary> CREATOR = new Creator<MovieSummary>() {
        @Override
        public MovieSummary createFromParcel(Parcel in) {
            return new MovieSummary(in);
        }

        @Override
        public MovieSummary[] newArray(int size) {
            return new MovieSummary[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) { return null; }
        int size = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(buffer);
        return buffer.array();
    }

    private Bitmap byteArrayToBitmap(byte[] bytes) {
        if (bytes == null) { return null; }
        return BitmapFactory.decodeByteArray(bytes, 0, 0);
    }

    public void writeToParcel(Parcel dest, int flags) {
        try {
            for(String nameString : namesString) {
                dest.writeString((String) MovieSummary.class.getDeclaredField(nameString).get(this));
            }
            for(String nameString : namesDetails) {
                dest.writeString((String) MovieSummary.class.getDeclaredField(nameString).get(this));
            }
            for(String nameString : namesBoolean) {
                dest.writeInt(((boolean)MovieSummary.class.getDeclaredField(nameString).get(this)) ? 1 : 0);
            }
            for(String nameString : namesList) {
                dest.writeList((List)MovieSummary.class.getDeclaredField(nameString).get(this));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues asContentValues() {
        ContentValues v = new ContentValues();
        try {
            for (String nameString : namesString) {
                v.put(nameString, (String) myClass.getDeclaredField(nameString).get(this));
            }
            for (String nameString : namesDetails) {
                v.put(nameString, (String) myClass.getDeclaredField(nameString).get(this));
            }
            for (String nameString : new String[]{"favorite"}) {
                v.put(nameString, (boolean) myClass.getDeclaredField(nameString).get(this) ? 1 : 0);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return v;
    }

    public boolean isValid() { return !movie_id.equals(INVALID_ID); }

    public String getAdult() {
        return adult;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public String getBackdropUrlString() {
        return IMAGE_BASE_URL + getBackdrop_path();
    }

    public URL getBackdropUrl() {
        URL url = null;
        if (isValid() && getBackdrop_path() != null) {
            try {
                url = new URL(getBackdropUrlString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public void setHasLocalBackdropBitmap(boolean bool) {
        bLocalBackdropBitmap = bool;
    }

    public String getBackdropKey() {
        return getBackdrop_path().replace("/", "_").replace(".jpg", "");
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean bool) {
        favorite = bool;
        // now update our content provider
        ContentValues favoriteValue = new ContentValues(1);
        favoriteValue.put("favorite", favorite);
        s_context.getContentResolver().update(PostersContract.PostersEntry.CONTENT_URI, favoriteValue, "movie_id = ? ", new String[]{movie_id});

        // tell the posterfragment adapter that things have changed
        GridView gridView = (GridView)MainActivity.getInstance().findViewById(R.id.gridview);
        ((PostersAdapter)gridView.getAdapter()).refreshFavorites();
    }

    public String getGenre_ids() {
        return genre_ids;
    }

    public String getMovieId() {
        return movie_id;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPopularity() {
        return popularity;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getPosterUrlString() {
        return IMAGE_BASE_URL + getPoster_path();
    }

    public URL getPosterUrl() {
        URL url = null;
        if (isValid() && getPoster_path() != null) {
            try {
                url = new URL(getPosterUrlString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public Bitmap getPosterBitmap() {
        Bitmap bitmap = null;
        if (bLocalPosterBitmap) {
            bitmap = SupplyBitmaps.getLocalBitmap(getPosterKey());
        }
        return bitmap;
    }

    public void setHasLocalPosterBitmap(boolean bool) {
        bLocalPosterBitmap = bool;
    }

    public String getPosterKey() {
        return getPoster_path().replace("/", "_").replace(".jpg", "");
    }

    public String getRuntime() { return runtime; }

    public String getRelease_date() {
        return release_date;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public String getTitle() {
        return title;
    }

    public List<Trailer> getTrailers() {
        return trailers;
    }

    public String getVideo() {
        return video;
    }

    public String getVote_average() {
        return vote_average;
    }

    public String getVote_count() {
        return vote_count;
    }

    static final String MOVIE_DETAIL_BASE_URL = "https://api.themoviedb.org/3/movie/";
    static final String APPEND_TO_RESPONSE = "append_to_response";
    static final String MOVIE_TRAILERS = "trailers";
    static final String MOVIE_REVIEWS = "reviews";

    static class FillInMovieDetails extends AsyncTask<MovieSummary, Integer, Long> {
        Context context;
        MovieSummary mMovieSummary;

        FillInMovieDetails(Context context) {
            this.context = context;
        }

        @Override
        protected Long doInBackground(MovieSummary... params) {
            mMovieSummary = params[0];

            grabMovieDetail();

            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
        }

        void grabMovieDetail() {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String apiKey = Utility.getPreferredApiKey(context);
            if (( apiKey == null ) || apiKey.equals("")) {
                return;
            }

            try {
                // construct url for themoviedb api, ask for either popularity or rating with the
                // highest first

                Uri builtUri = Uri.parse(MOVIE_DETAIL_BASE_URL + mMovieSummary.getMovieId()).buildUpon()
                        .appendQueryParameter(SyncWorker.API_KEY, apiKey)
                        .appendQueryParameter(APPEND_TO_RESPONSE, MOVIE_TRAILERS + "," + MOVIE_REVIEWS)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }

                processJSON(buffer.toString());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error title " + mMovieSummary.getTitle());
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }

        void processJSON(String strWhole) {
            try {
                JSONObject jsonWhole = new JSONObject(strWhole);
                // we are interested in the runtime
                mMovieSummary.runtime = jsonWhole.getString("runtime");

                // we are interested in the trailers too
                List<Trailer> trailers = new ArrayList<Trailer>();
                JSONObject jsonTrailers = jsonWhole.getJSONObject("trailers");
                JSONArray jsonArray = jsonTrailers.getJSONArray("youtube");
                for (int i=0; i < jsonArray.length(); i++) {
                    JSONObject t = jsonArray.getJSONObject(i);
                    Trailer trailer = new Trailer();
                    trailer.setYoutube(true);
                    trailer.setTitle(t.getString("name"));
                    trailer.setSize(t.getString("size"));
                    trailer.setSource(t.getString("source"));
                    trailer.setType(t.getString("type"));
                    trailers.add(trailer);
                }
                mMovieSummary.trailers = trailers;

                // and get the reviews if they exist
                List<Review> reviews = new ArrayList<Review>();
                JSONObject jsonReviewObj = jsonWhole.getJSONObject("reviews");
                JSONArray jsonReviews = jsonReviewObj.getJSONArray("results");
                for (int i=0; i < jsonReviews.length(); i++) {
                    JSONObject r = jsonReviews.getJSONObject(i);
                    Review review = new Review();
                    review.setReview_id(r.getString("id"));
                    review.setAuthor(r.getString("author"));
                    review.setContent(r.getString("content"));
                    review.setUrl(r.getString("url"));
                    reviews.add(review);
                }
                mMovieSummary.reviews = reviews;

                // now update the content resolver
                String movie_id = mMovieSummary.getMovieId();
                if(trailers.size() > 0) {
                    Vector<ContentValues> vectorContentValues = new Vector<ContentValues>(trailers.size());
                    for(Trailer trailer : trailers) {
                        ContentValues cv = trailer.asContentValues(movie_id);
                        vectorContentValues.add(cv);
                    }
                    ContentValues [] arrayContentValues = new ContentValues[trailers.size()];
                    vectorContentValues.toArray(arrayContentValues);
                    s_context.getContentResolver().bulkInsert(PostersContract.TrailersEntry.CONTENT_URI, arrayContentValues);
                }

                if (reviews.size() > 0) {
                    Vector<ContentValues> vectorContentValues = new Vector<ContentValues>(reviews.size());
                    for(Review review : reviews) {
                        ContentValues cv = review.asContentValues(movie_id);
                        vectorContentValues.add(cv);
                    }
                    ContentValues [] arrayContentValues = new ContentValues[reviews.size()];
                    vectorContentValues.toArray(arrayContentValues);
                    s_context.getContentResolver().bulkInsert(PostersContract.ReviewsEntry.CONTENT_URI, arrayContentValues);
                }

                ContentValues runtimeValue = new ContentValues(1);
                runtimeValue.put("runtime", mMovieSummary.getRuntime());
                s_context.getContentResolver().update(PostersContract.PostersEntry.CONTENT_URI, runtimeValue, "movie_id = ? ", new String[]{movie_id});


           } catch (JSONException e) {
                e.printStackTrace();
           }
        }
    }

    public static class Trailer implements Parcelable {
        boolean youtube = false;
        boolean quicktime = false;
        String title;
        String size;
        String source;
        String type;

        public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
            @Override
            public Trailer createFromParcel(Parcel in) {
                return new Trailer(in);
            }

            @Override
            public Trailer[] newArray(int size) {
                return new Trailer[size];
            }
        };

        public boolean isQuicktime() {
            return quicktime;
        }

        public void setQuicktime(boolean quicktime) {
            this.quicktime = quicktime;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getURLString() {
            return "https://www.youtube.com/watch?v=" + getSource();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isYoutube() {
            return youtube;
        }

        public void setYoutube(boolean youtube) {
            this.youtube = youtube;
        }

        public Trailer() {

        }

        public Trailer (Cursor cursor) {
            int index = cursor.getColumnIndex("youtube");
            if (index >= 0) {
                youtube = cursor.getInt(index) != 0;
            }
            index = cursor.getColumnIndex("quicktime");
            if (index >= 0) {
                quicktime = cursor.getInt(index) != 0;
            }
            index = cursor.getColumnIndex("title");
            if (index >= 0) {
                title = cursor.getString(index);
            }
            index = cursor.getColumnIndex("size");
            if (index >= 0) {
                size = cursor.getString(index);
            }
            index = cursor.getColumnIndex("source");
            if (index >= 0) {
                source = cursor.getString(index);
            }
            index = cursor.getColumnIndex("type");
            if (index >= 0) {
                type = cursor.getString(index);
            }
        }


        public Trailer(Parcel parcel) {
            youtube = (parcel.readInt() != 0) ? true : false;
            quicktime = (parcel.readInt() != 0) ? true : false;
            title = parcel.readString();
            size = parcel.readString();
            source = parcel.readString();
            type = parcel.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(isYoutube() ? 1 : 0);
            dest.writeInt(isQuicktime() ? 1 : 0);
            dest.writeString(getTitle());
            dest.writeString(getSize());
            dest.writeString(getSource());
            dest.writeString(getType());
        }

        public ContentValues asContentValues(String movie_id) {
            ContentValues v = new ContentValues();

            v.put("movie_id", movie_id);
            v.put("youtube", youtube ? 1 : 0);
            v.put("quicktime", quicktime ? 1 : 0);
            v.put("title", getTitle());
            v.put("size", getSize());
            v.put("source", getSource());
            v.put("type", getType());

            return v;
        }

    }

    public static class Review implements Parcelable {
        String review_id;
        String author;
        String content;
        String url;

        public Review() {

        }

        protected Review(Parcel in) {
            review_id = in.readString();
            author = in.readString();
            content = in.readString();
            url = in.readString();
        }

        public Review (Cursor cursor) {
            int index = cursor.getColumnIndex("review_id");
            if (index >= 0) {
                review_id = cursor.getString(index);
            }
            index = cursor.getColumnIndex("author");
            if (index >= 0) {
                author = cursor.getString(index);
            }
            index = cursor.getColumnIndex("content");
            if (index >= 0) {
                content = cursor.getString(index);
            }
            index = cursor.getColumnIndex("url");
            if (index >= 0) {
                url = cursor.getString(index);
            }
        }

        public static final Creator<Review> CREATOR = new Creator<Review>() {
            @Override
            public Review createFromParcel(Parcel in) {
                return new Review(in);
            }

            @Override
            public Review[] newArray(int size) {
                return new Review[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(getReview_id());
            dest.writeString(getAuthor());
            dest.writeString(getContent());
            dest.writeString(getUrl());

        }

        public ContentValues asContentValues(String movie_id) {
            ContentValues v = new ContentValues();

            v.put("movie_id", movie_id);
            v.put("review_id", getReview_id());
            v.put("author", getAuthor());
            v.put("content", getContent());
            v.put("url", getUrl());

            return v;
        }


        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getReview_id() {
            return review_id;
        }

        public void setReview_id(String review_id) {
            this.review_id = review_id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
