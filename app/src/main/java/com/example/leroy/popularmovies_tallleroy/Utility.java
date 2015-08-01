package com.example.leroy.popularmovies_tallleroy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by LeRoy on 7/31/2015.
 */
public class Utility {

    public static String getPreferredSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_order_list),
                context.getString(R.string.pref_entry_sort_order_popularity));
    }

    public static String getPreferredApiKey (Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_api_key_text), null);
    }

}
