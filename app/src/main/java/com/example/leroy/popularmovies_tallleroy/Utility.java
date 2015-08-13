package com.example.leroy.popularmovies_tallleroy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by LeRoy on 7/31/2015.
 */
public class Utility {
    private static final String MEMORY_CACHE_FILENAME_PREFIX = "LruCache_memoryCache";

    public static String getPreferredSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_order_list),
                context.getString(R.string.pref_entry_sort_order_popularity));
    }

    public static String getPreferredApiKey (Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_api_key_text), null);
    }

    private static String getMemoryCacheFileName() {
        return MEMORY_CACHE_FILENAME_PREFIX;
    }

     public static void saveBitmapToFile(String key, Bitmap bitmap, Context context) {
        String filename = getMemoryCacheFileName() + key;
        try {
            FileOutputStream os = context.openFileOutput(filename, context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    public static Bitmap getBitmapFromFile(String key, Context context) {
        Bitmap bitmap = null;
        String filename = getMemoryCacheFileName() + key;
        try {
            FileInputStream is = context.openFileInput(filename);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return bitmap;
    }

    public static class CleanupFiledBitmaps extends AsyncTask<List<MovieSummary>, Integer, Long> {
        Context context;

        public CleanupFiledBitmaps(Context context) {
            this.context = context;
        }

        @Override
        protected Long doInBackground(List<MovieSummary>... params) {
            List<MovieSummary> movieSummaries = params[0];
            // create a set of keys from this List of MovieSummaries
            Set<String> keySet = new HashSet<String>(movieSummaries.size());
            for(MovieSummary movieSummary : movieSummaries) {
                keySet.add(movieSummary.getPosterKey());
            }
            // now delete any local poster file that has a filename that not in the keySet
            File filesDir = new File(context.getFilesDir().toString());
            if (filesDir != null) {
                File[] files = filesDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String filename = f.getName();
                        if(filename.startsWith(MEMORY_CACHE_FILENAME_PREFIX)) {
                            String tag = filename.replace(MEMORY_CACHE_FILENAME_PREFIX, "");
                            if (keySet.contains(tag)) {
                                // keep this bitmap it is current
                            } else {
                                // this bitmap is no longer needed
                                f.delete();
                            }
                        }
                    }
                }
            }

            return null;
        }
    }

}
