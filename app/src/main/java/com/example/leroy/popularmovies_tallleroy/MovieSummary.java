package com.example.leroy.popularmovies_tallleroy;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Created by LeRoy on 8/1/2015.
 */
public class MovieSummary implements Parcelable {
    public static String INVALID_ID = "-1";
    public static String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";

    public static void setActivity(Activity activity) {
        // run this once at the first time the class it used
        // determine the screen size and update the IMAGE_BASE_URL
        // to keep the onscreen size right
        if( IMAGE_BASE_URL.endsWith("/")) {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

            int density = dm.densityDpi;
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
    String poster_path;
    String popularity;
    String title;
    String video;
    String vote_average;
    String vote_count;
    Bitmap backdrop_bitmap = null;
    Bitmap poster_bitmap = null;

    private static Class<MovieSummary> myClass = MovieSummary.class;

    // make sure all of the field names are in these 'namesXXX' arrays by type
    private static String [] namesBitmap = {"backdrop_bitmap", "poster_bitmap"};
    private static String [] namesString = {"adult", "backdrop_path", "genre_ids", "original_language", "original_title",
            "overview", "release_date", "poster_path", "popularity", "title", "video", "vote_average", "vote_count", "movie_id"};

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
//            for(String nameString : namesBitmap) {
//                myClass.getDeclaredField(nameString).set(this, byteArrayToBitmap(in.createByteArray()));
//            }
            for(String nameString : namesString) {
               myClass.getDeclaredField(nameString).set(this, in.readString());
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
        return BitmapFactory.decodeByteArray(bytes,0,0);
    }

    public void writeToParcel(Parcel dest, int flags) {
        try {
//            for(String nameString : namesBitmap) {
//                dest.writeByteArray(bitmapToByteArray((Bitmap)MovieSummary.class.getDeclaredField(nameString).get(this)));
//            }
            for(String nameString : namesString) {
                dest.writeString((String) MovieSummary.class.getDeclaredField(nameString).get(this));
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

    public Bitmap getBackdropBitmap() {
        return backdrop_bitmap;
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
        return poster_bitmap;
    }

    public String getRelease_date() {
        return release_date;
    }

    public String getTitle() {
        return title;
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

    public void setPoster_bitmap(Bitmap poster_bitmap) {
        this.poster_bitmap = poster_bitmap;
    }

    public void setBackdrop_bitmap(Bitmap backdrop_bitmap) {
        this.backdrop_bitmap = backdrop_bitmap;
    }

}
