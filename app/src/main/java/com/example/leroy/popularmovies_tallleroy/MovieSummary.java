package com.example.leroy.popularmovies_tallleroy;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;

/**
 * Created by LeRoy on 8/1/2015.
 */
public class MovieSummary implements Parcelable {
    public static int INVALID_ID = -1;
    public static String IMAGE_URL = "http://image.tmdb.org/t/p/w185";

    boolean adult;
    String backdrop_path;
    int [] genre_ids;
    int id = INVALID_ID;
    String original_language;
    String original_title;
    String overview;
    Date release_date;
    String poster_path;
    double popularity;
    String title;
    boolean video;
    double vote_average;
    int vote_count;

    // make sure all of the field names are in these 'namesXXX' arrays by type
    private static String [] namesBoolean = {"adult", "video"};
    private static String [] namesString = {"backdrop_path", "original_language", "original_title", "overview", "poster_path", "title"};
    private static String [] namesInt = {"id", "vote_count"};
    private static String [] namesIntArray = {"genre_ids"};
    private static String [] namesDate = {"release_date"};
    private static String [] namesDouble = {"popularity", "vote_average"};

    public MovieSummary(JSONObject movie) {
        try {
            for (String nameString : namesBoolean) {
                MovieSummary.class.getDeclaredField(nameString).set(this, movie.getBoolean(nameString));
            }
            for (String nameString : namesDate) {
                MovieSummary.class.getDeclaredField(nameString).set(this, Date.valueOf(movie.getString(nameString)));
            }
            for (String nameString : namesDouble) {
                MovieSummary.class.getDeclaredField(nameString).set(this, movie.getDouble(nameString));
            }
            for (String nameString : namesInt) {
                MovieSummary.class.getDeclaredField(nameString).set(this, movie.getInt(nameString));
            }
            for (String nameString : namesIntArray) {
                JSONArray jsonArray = movie.getJSONArray(nameString);
                int size = jsonArray.length();
                int [] intArray = new int[size];
                for (int i = 0; i < size; i++) {
                   intArray[i] = jsonArray.getInt(i);
                }
                MovieSummary.class.getDeclaredField(nameString).set(this, intArray);
            }
            for(String nameString : namesString) {
                MovieSummary.class.getDeclaredField(nameString).set(this, movie.getString(nameString));
            }
        } catch (JSONException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected MovieSummary(Parcel in) {

        try {
            for (String nameString : namesBoolean) {
                MovieSummary.class.getDeclaredField(nameString).set(this, in.readInt() != 0);
            }
            for (String nameString : namesDate) {
                MovieSummary.class.getDeclaredField(nameString).set(this, Date.valueOf(in.readString()));
            }
            for (String nameString : namesDouble) {
                MovieSummary.class.getDeclaredField(nameString).set(this, in.readDouble());
            }
            for (String nameString : namesInt) {
                MovieSummary.class.getDeclaredField(nameString).set(this, in.readInt());
            }
            for (String nameString : namesIntArray) {
                int size = in.readInt();
                int [] array = new int[size];
                in.readIntArray(array);
                MovieSummary.class.getDeclaredField(nameString).set(this, array);
            }
            for(String nameString : namesString) {
                MovieSummary.class.getDeclaredField(nameString).set(this, in.readString());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
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

    public void writeToParcel(Parcel dest, int flags) {
        try {
            for (String nameString : namesBoolean) {
                dest.writeInt(MovieSummary.class.getDeclaredField(nameString).getBoolean(this) ? 1 : 0);
            }
            for (String nameString : namesDate) {
                String dateString = MovieSummary.class.getDeclaredField(nameString).get(this).toString();
                dest.writeString(dateString);
            }
            for (String nameString : namesDouble) {
                dest.writeDouble(MovieSummary.class.getDeclaredField(nameString).getDouble(this));
            }
            for (String nameString : namesInt) {
                dest.writeInt(MovieSummary.class.getDeclaredField(nameString).getInt(this));
            }
            for (String nameString : namesIntArray) {
                int [] array = (int [])MovieSummary.class.getDeclaredField(nameString).get(this);
                int size = array.length;
                dest.writeInt(size);
                dest.writeIntArray(array);
            }
            for(String nameString : namesString) {
                dest.writeString((String)MovieSummary.class.getDeclaredField(nameString).get(this));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
     }

    public boolean isAdult() {
        return adult;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public URL getBackdropURL() {
        URL url = null;
        if (getBackdrop_path() != null) {
            try {
                url = new URL(IMAGE_URL + getBackdrop_path());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public int[] getGenre_ids() {
        return genre_ids;
    }

    public int getId() {
        return id;
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

    public double getPopularity() {
        return popularity;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public URL getPosterURL() {
        URL url = null;
        if (getPoster_path() != null) {
            try {
                url = new URL(IMAGE_URL + getPoster_path());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    public Date getRelease_date() {
        return release_date;
    }

    public String getTitle() {
        return title;
    }

    public boolean isVideo() {
        return video;
    }

    public double getVote_average() {
        return vote_average;
    }

    public int getVote_count() {
        return vote_count;
    }
}
