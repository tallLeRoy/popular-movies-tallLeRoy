package com.example.leroy.popularmovies_tallleroy;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.Arrays;

/**
 * Created by LeRoy on 8/1/2015.
 */
public class MovieSummary implements Parcelable {
    public static int INVALID_ID = -1;
    public static String IMAGE_URL = "https://image.tmdb.org/t/p/w185";

    boolean adult;
    String backdrop_path;
    int [] genre_ids;
    int movie_id = INVALID_ID;
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
    int sort_order;

    private static Class<MovieSummary> myClass = MovieSummary.class;

    // make sure all of the field names are in these 'namesXXX' arrays by type
    private static String [] namesBoolean = {"adult", "video"};
    private static String [] namesString = {"backdrop_path", "original_language", "original_title", "overview", "poster_path", "title"};
    private static String [] namesInt = {"movie_id", "vote_count", "sort_order"};
    private static String [] namesIntArray = {"genre_ids"};
    private static String [] namesDate = {"release_date"};
    private static String [] namesDouble = {"popularity", "vote_average"};

    public MovieSummary(JSONObject movie, int sort_order) {

        try {
            for (String nameString : namesBoolean) {
               myClass.getDeclaredField(nameString).set(this, movie.getBoolean(nameString));
            }
            for (String nameString : namesDate) {
               myClass.getDeclaredField(nameString).set(this, Date.valueOf(movie.getString(nameString)));
            }
            for (String nameString : namesDouble) {
               myClass.getDeclaredField(nameString).set(this, movie.getDouble(nameString));
            }
            for (String nameString : namesInt) {
                if (nameString.equals("movie_id")) {
                    // hold off on setting movie_id until the end... sets object as valid
                } else if (nameString.equals("sort_order")) {
                    // special case, sort_order is not included in the JSON
                    myClass.getDeclaredField(nameString).set(this, sort_order);
                } else {
                    myClass.getDeclaredField(nameString).set(this, movie.getInt(nameString));
                }
            }
            for (String nameString : namesIntArray) {
                JSONArray jsonArray = movie.getJSONArray(nameString);
                int size = jsonArray.length();
                int [] intArray = new int[size];
                for (int i = 0; i < size; i++) {
                   intArray[i] = jsonArray.getInt(i);
                }
               myClass.getDeclaredField(nameString).set(this, intArray);
            }
            for(String nameString : namesString) {
               myClass.getDeclaredField(nameString).set(this, movie.getString(nameString));
            }

            // finally set the movie_id from id in the JSON to mark the object as valid
            // note the name change to movie_id, it will be movie_id from here on out
            myClass.getDeclaredField("movie_id").set(this, movie.getInt("id"));

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
            for (String nameString : namesBoolean) {
               myClass.getDeclaredField(nameString).set(this, in.readInt() != 0);
            }
            for (String nameString : namesDate) {
               myClass.getDeclaredField(nameString).set(this, Date.valueOf(in.readString()));
            }
            for (String nameString : namesDouble) {
               myClass.getDeclaredField(nameString).set(this, in.readDouble());
            }
            for (String nameString : namesInt) {
               myClass.getDeclaredField(nameString).set(this, in.readInt());
            }
            for (String nameString : namesIntArray) {
                int size = in.readInt();
                int [] array = new int[size];
                in.readIntArray(array);
               myClass.getDeclaredField(nameString).set(this, array);
            }
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
            for (String nameString : namesBoolean) {
                if ( v.containsKey(nameString) ) {
                   myClass.getDeclaredField(nameString).set(this, v.getAsInteger(nameString) != 0);
                }
            }
            for (String nameString : namesDate) {
                if ( v.containsKey(nameString) ) {
                   myClass.getDeclaredField(nameString).set(this, Date.valueOf(v.getAsString(nameString)));
                }
            }
            for (String nameString : namesDouble) {
                if ( v.containsKey(nameString) ) {
                   myClass.getDeclaredField(nameString).set(this, v.getAsDouble(nameString));
                }
            }
            for (String nameString : namesInt) {
                if ( v.containsKey(nameString) ) {
                   myClass.getDeclaredField(nameString).set(this, v.getAsInteger(nameString));
                }
            }
            for (String nameString : namesIntArray) {
                if ( v.containsKey(nameString) ) {
                    String[] strInt = v.getAsString(nameString).replace("[", "").replace("]","").split(", ");
                    int size = strInt.length;
                    int [] array = new int[size];
                    for (int i = 0; i < size; i++) {
                        array[i] = Integer.parseInt(strInt[i]);
                    }
                   myClass.getDeclaredField(nameString).set(this, array);
                }
            }
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
                String dateString =myClass.getDeclaredField(nameString).get(this).toString();
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

    public ContentValues asContentValues() {
        ContentValues v = new ContentValues();
        try {
            for (String nameString : namesBoolean) {
                v.put(nameString,myClass.getDeclaredField(nameString).getBoolean(this) ? 1 : 0);
            }
            for (String nameString : namesDate) {
                String dateString =myClass.getDeclaredField(nameString).get(this).toString();
                v.put(nameString, dateString);
            }
            for (String nameString : namesDouble) {
                v.put(nameString,myClass.getDeclaredField(nameString).getDouble(this));
            }
            for (String nameString : namesInt) {
               v.put(nameString,myClass.getDeclaredField(nameString).getInt(this));
            }
            for (String nameString : namesIntArray) {
                int[] array = (int[])myClass.getDeclaredField(nameString).get(this);
                v.put(nameString, Arrays.toString(array));
            }
            for (String nameString : namesString) {
                v.put(nameString, (String)myClass.getDeclaredField(nameString).get(this));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return v;
    }

    public boolean isValid() { return movie_id != INVALID_ID; }

    public boolean isAdult() {
        return adult;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public Uri getBackdropUri() {
        Uri uri = null;
        if (isValid() && getBackdrop_path() != null) {
            uri = Uri.parse(IMAGE_URL + getBackdrop_path());
        }
        return uri;
    }

    public int[] getGenre_ids() {
        return genre_ids;
    }

    public int getMovieId() {
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

    public double getPopularity() {
        return popularity;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public Uri getPosterUri() {
        Uri uri = null;
        if (isValid() && getPoster_path() != null) {
            uri = Uri.parse(IMAGE_URL + getPoster_path());
        }
        return uri;
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
