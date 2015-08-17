package com.example.leroy.popularmovies_tallleroy.sync;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.leroy.popularmovies_tallleroy.MainActivity;
import com.example.leroy.popularmovies_tallleroy.MovieSummary;
import com.example.leroy.popularmovies_tallleroy.R;
import com.example.leroy.popularmovies_tallleroy.Utility;
import com.example.leroy.popularmovies_tallleroy.data.PostersContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by LeRoy Krueger on 8/1/2015.
 */
public class SyncWorker {
    public static final String LOG_TAG = ".sync." + SyncWorker.class.getSimpleName();
    static final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
    public static final String API_KEY = "api_key";
    static final String SORT_BY = "sort_by";
    static final String RELEASE_DATE_LTE = "primary_release_date.lte";
    static final String VOTE_COUNT_GTE = "vote_count.gte";
//    static final String POPULARITY = "popularity.desc";
    static final String NEWEST_RELEASES = "primary_release_date.desc";

    static final String CURRENT_POSTERS = PostersContract.PostersEntry.CURRENT_POSTERS;
//    static final String POSTER_BY_MOVIE_ID_SELECTION = PostersContract.PostersEntry.POSTER_BY_MOVIE_ID_SELECTION;

    // return true if the database was not refreshed today, themoviedb is updated daily
    public static boolean isSyncRequired(Context context) {
        boolean bSyncRequired = false;

        // are there records in the contentprovider for today with this sort order?
        Cursor cursor = context.getContentResolver().query(PostersContract.PostersEntry.CONTENT_URI, null, CURRENT_POSTERS, null, null);

        if (cursor.getCount() == 0) {
            bSyncRequired = true;
        }

        cursor.close();

        return bSyncRequired;
    }

    public static List<String> getCurrentMovieIds() {
        List<String> movieList = new ArrayList<String>(20);

        Cursor cursor = MainActivity.getContext().getContentResolver().query(PostersContract.PostersEntry.CONTENT_URI, new String[]{"movie_id"}, null, null, null);

        while(cursor.moveToNext()) {
            movieList.add(cursor.getString(cursor.getColumnIndex("movie_id")));
        }

        return movieList;
    }

    public static void cleanDatabase(Context context) {
        // wipe out the whole table
        context.getContentResolver().delete(PostersContract.PostersEntry.CONTENT_URI, null, null);
    }

    public static void performSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult, Context context) {
        Log.v(LOG_TAG, "Starting sync");

        if (MainActivity.CLEAN_LOCAL_FILES) {
            // for debug, load the data anytime
            cleanDatabase(context);
        }

//        String sortValue = POPULARITY;
        String sortValue = NEWEST_RELEASES;

        if (!isSyncRequired(context)) {
            Log.v(LOG_TAG, "Sync not needed, we have the current " + sortValue + " data");
            return;
        }



        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String apiKey = Utility.getPreferredApiKey(context);
        if (( apiKey == null ) || apiKey.equals("")) {
            Log.d(LOG_TAG, "Sync error, apiKey is null or empty");
            return;
        }

        try {
            // construct URL for themoviedb api, ask for either popularity or rating with the
            // highest first

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY, sortValue)
                    .appendQueryParameter(RELEASE_DATE_LTE,
                            new SimpleDateFormat("yyyy.MM.dd").format(new java.util.Date()))
                    .appendQueryParameter(VOTE_COUNT_GTE, "50")
                    .appendQueryParameter(API_KEY, apiKey)
                    .appendQueryParameter("append_to_response", "trailers,runtime")
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
//            Log.v(LOG_TAG, buffer.toString());

            processJSON(buffer.toString(), context);
        } catch (IOException e) {
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

        // tell the sharedpref listeners about the new data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String newDataLoaded = context.getResources().getString(R.string.pref_new_data_loaded) + new Random();
        editor.putString(newDataLoaded, "true");
        editor.commit();

        Log.v(LOG_TAG, "Ending Sync");
        return;
    }

    private static void processJSON(String strWhole, Context context) {
        try {
            JSONObject jsonWhole = new JSONObject(strWhole);
            JSONArray results = jsonWhole.getJSONArray("results");
            Vector<ContentValues> cVVector = new Vector<ContentValues>(results.length());

            List<String> current_movie_ids = getCurrentMovieIds();
            List<String> new_movie_ids = new ArrayList<String>(results.length());

            for(int i=0; i < results.length(); i++) {
                MovieSummary ms = new MovieSummary(results.getJSONObject(i));
                if (ms.isValid()) {
                    String movie_id = ms.getMovieId();
                    new_movie_ids.add(movie_id);
                    if (!current_movie_ids.contains(movie_id)) {
                        // we don't need to reinsert into the content provider
                        cVVector.add(ms.asContentValues());
                    }
                }
            }

            // now populate our local database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                // delete the set of posters not in our latest list
                String selection = "movie_id NOT IN ( ? )";
                String[] args = new String[]{ new_movie_ids.toString().replace(", ", "', '").replace("[", "'").replace("]", "'") };
                context.getContentResolver().delete(PostersContract.PostersEntry.CONTENT_URI, selection, args);
                context.getContentResolver().delete(PostersContract.TrailersEntry.CONTENT_URI, selection, args);
                context.getContentResolver().delete(PostersContract.ReviewsEntry.CONTENT_URI, selection, args);
                // insert the new posters table
                context.getContentResolver().bulkInsert(PostersContract.PostersEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
