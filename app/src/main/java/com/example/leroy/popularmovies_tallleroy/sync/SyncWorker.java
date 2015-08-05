package com.example.leroy.popularmovies_tallleroy.sync;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.leroy.popularmovies_tallleroy.MovieSummary;
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
import java.util.Vector;

/**
 * Created by LeRoy Krueger on 8/1/2015.
 */
public class SyncWorker {
    public static final String LOG_TAG = ".sync." + SyncWorker.class.getSimpleName();
    static final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
    static final String API_KEY = "api_key";
    static final String SORT_BY = "sort_by";
    static final String POPULARITY = "popularity.desc";
    static final String RATING = "vote_average.desc";
    public static final int SORT_ORDER_POPULARITY = 1;
    public static final int SORT_ORDER_RATING = 2;

    public static final String POSTER_BY_MOVIE_ID_SELECTION = PostersContract.PostersEntry.TABLE_NAME + "." + PostersContract.PostersEntry.COLUMN_MOVIE_ID + " = ? ";
    public static final String POSTERS_BY_SORT_ORDER_VALUE = PostersContract.PostersEntry.TABLE_NAME + "." + PostersContract.PostersEntry.COLUMN_SORT_ORDER + " = ? ";
    // give us the set of posters that were inserted before today and have the same sort_order
    public static final String CURRENT_POSTERS_BY_SORT_ORDER_VALUE = POSTERS_BY_SORT_ORDER_VALUE +
            " AND " +  PostersContract.PostersEntry.TABLE_NAME + "." + PostersContract.PostersEntry.COLUMN_INSERT_DATE + " = CURRENT_DATE ";
    public static final String POSTERS_QUERY_SORT_ORDER = PostersContract.PostersEntry.TABLE_NAME + "." + "_ID ASC "; 


    // return true if the database was not refreshed today, themoviedb is updated daily
    public static boolean isSyncRequired(Context context, int sortValueId) {
        boolean bSyncRequired = false;

        // are there records in the contentprovider for today with this sort order?
        Cursor cursor = context.getContentResolver().query(PostersContract.PostersEntry.CONTENT_URI,
                null, CURRENT_POSTERS_BY_SORT_ORDER_VALUE, new String[]{Integer.toString(sortValueId)}, POSTERS_QUERY_SORT_ORDER);

        if (cursor.getCount() == 0) {
            bSyncRequired = true;
        }

        return bSyncRequired;
    }

    public static void cleanDatabase(Context context) {
        // wipe out the whole table
        context.getContentResolver().delete(PostersContract.PostersEntry.CONTENT_URI, null, null);
    }

    public static void performSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult, Context context) {
        Log.d(LOG_TAG, "Starting sync");

//        // for debug, load the data anytime
//        cleanDatabase(context);

        String sortValue = POPULARITY;
        int sortValueId = SORT_ORDER_POPULARITY;
        if (Utility.getPreferredSortOrder(context).equals(Integer.toString(SORT_ORDER_RATING))) {
            sortValue = RATING;
            sortValueId = SORT_ORDER_RATING;
        }

        if (!isSyncRequired(context, sortValueId)) {
            Log.d(LOG_TAG, "Sync not needed, we have the current " + sortValue + " data");
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

//
//        // Will contain the raw JSON response as a string.
//
        try {
            // construct URL for themoviedb api, ask for either popularity or rating with the
            // highest first

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY, sortValue)
                    .appendQueryParameter(API_KEY, apiKey)
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
//            Log.d(LOG_TAG, buffer.toString());

            processJSON(buffer.toString(), context, sortValueId);
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
        Log.d(LOG_TAG,"Ending Sync");
        return;
    }

    private static void processJSON(String strWhole, Context context, int sortValueId) {
        try {
            JSONObject jsonWhole = new JSONObject(strWhole);
            JSONArray results = jsonWhole.getJSONArray("results");
            Vector<ContentValues> cVVector = new Vector<ContentValues>(results.length());

            for(int i=0; i < results.length(); i++) {
                MovieSummary ms = new MovieSummary(results.getJSONObject(i), sortValueId);
                if (ms.isValid()) {
                    cVVector.add(ms.asContentValues());
                }
            }

            // now populate our local database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                // delete the current set of posters for this mSortOrder
                context.getContentResolver().delete(PostersContract.PostersEntry.CONTENT_URI,
                        POSTERS_BY_SORT_ORDER_VALUE, new String[]{Integer.toString(sortValueId)} );
                // insert the new posters table
                context.getContentResolver().bulkInsert(PostersContract.PostersEntry.CONTENT_URI, cvArray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
