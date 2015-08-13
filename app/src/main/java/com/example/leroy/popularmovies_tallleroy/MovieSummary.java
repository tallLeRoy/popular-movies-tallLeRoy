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

/**
 * Created by LeRoy on 8/1/2015.
 */
public class MovieSummary implements Parcelable {
    public static final String LOG_TAG = MovieSummary.class.getSimpleName();
    public static String INVALID_ID = "-1";
    public static String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    static Context s_context;

    public static void setActivity(Activity activity) {
        // run this once at the first time the class it used
        // determine the screen size and update the IMAGE_BASE_URL
        // to keep the onscreen size right
        s_context = activity;

        if( IMAGE_BASE_URL.endsWith("/")) {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

            int density = dm.densityDpi;
            if (!MainActivity.RUN_IN_EMULATOR.equals("true")) {
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
    String genre_ids;
    String movie_id = INVALID_ID;
    String original_language;
    String original_title;
    String overview;
    String release_date;
    String runtime;
    String poster_path;
    String popularity;
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
    private static String [] namesBoolean = {"bLocalBackdropBitmap", "bLocalPosterBitmap"};
    private static String [] namesList = {"trailers"};

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

    public MovieSummary(ContentValues v) {
        try {
//            for(String nameString : namesBitmap) {
//                if(v.containsKey(nameString)) {
//                    myClass.getDeclaredField(nameString).set(this, byteArrayToBitmap(v.getAsByteArray(nameString)));
//                }
//            }
            for(String nameString : namesString) {
                if ( v.containsKey(nameString) ) {
                   myClass.getDeclaredField(nameString).set(this, v.getAsString(nameString));
                }
            }
            for(String nameString : namesDetails) {
                if ( v.containsKey(nameString) ) {
                    myClass.getDeclaredField(nameString).set(this, v.getAsString(nameString));
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public MovieSummary(Cursor cursor) {
        try {
//            for(String nameString : namesBitmap) {
//                int index = cursor.getColumnIndex(nameString);
//                if (index >= 0) {
//                    myClass.getDeclaredField(nameString).set(this, byteArrayToBitmap(cursor.getBlob(index)));
//                }
//            }
//
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

            // we are created from a SQL call, so fill up the details using a background task
            new FillInMovieDetails(s_context).execute(this);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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
//            for (String nameString : namesBitmap) {
//                v.put(nameString, bitmapToByteArray((Bitmap)myClass.getDeclaredField(nameString).get(this)));
//            }
            for (String nameString : namesString) {
                v.put(nameString, (String) myClass.getDeclaredField(nameString).get(this));
            }
            for (String nameString : namesDetails) {
                v.put(nameString, (String) myClass.getDeclaredField(nameString).get(this));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return v;
    }

    public void provideDetails() {
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

//    public Bitmap getBackdropBitmap() {
//        return backdrop_bitmap;
//    }
    public void setHasLocalBackdropBitmap(boolean bool) {
        bLocalBackdropBitmap = bool;
    }

    public String getBackdropKey() {
        return getBackdrop_path().replace("/", "_").replace(".jpg", "");
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
//        return poster_bitmap;
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

//    public void setPoster_bitmap(Bitmap poster_bitmap) {
//        this.poster_bitmap = poster_bitmap;
//    }
//
//    public void setBackdrop_bitmap(Bitmap backdrop_bitmap) {
//        this.backdrop_bitmap = backdrop_bitmap;
//    }

    static final String MOVIE_DETAIL_BASE_URL = "https://api.themoviedb.org/3/movie/";
    static final String APPEND_TO_RESPONSE = "append_to_response";
    static final String MOVIE_TRAILERS = "trailers";

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
            if (mMovieSummary.bLocalPosterBitmap) {
                return;
            }
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String apiKey = Utility.getPreferredApiKey(context);
            if (( apiKey == null ) || apiKey.equals("")) {
                return;
            }

            try {
                // construct URL for themoviedb api, ask for either popularity or rating with the
                // highest first

                Uri builtUri = Uri.parse(MOVIE_DETAIL_BASE_URL + mMovieSummary.getMovieId()).buildUpon()
                        .appendQueryParameter(SyncWorker.API_KEY, apiKey)
                        .appendQueryParameter(APPEND_TO_RESPONSE, MOVIE_TRAILERS)
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
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    static class Trailer implements Parcelable {
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
    }
}
