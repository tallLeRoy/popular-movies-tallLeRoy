package com.example.leroy.popularmovies_tallleroy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

public class SupplyBitmaps extends AsyncTask<List<MovieSummary>, Integer, List<MovieSummary>> {
    protected AsyncSupplyBitmapResponse response;
    protected static LruCache<String, Bitmap> s_memoryCache = null;
    protected static DiskLruCache s_diskCache = null;
    protected static Object s_cacheLock = new Object();

    public SupplyBitmaps(AsyncSupplyBitmapResponse response) {
        this.response = response;
    }

    @Override
    protected List<MovieSummary> doInBackground(List<MovieSummary>... params) {
        List<MovieSummary> movieSummaries = params[0];
        for(MovieSummary movieSummary : movieSummaries) {
            if (movieSummary.getPosterBitmap() == null) {
                synchronized (s_cacheLock) {
                    String key = movieSummary.getPosterUrlString();
                    Bitmap bitmap = null;
                    LruCache<String, Bitmap> memoryCache = getMemoryCache(null);
                    if (memoryCache != null) {
                        bitmap = memoryCache.get(key);
                    }
                    if (bitmap == null) {
                        URL posterUrl = movieSummary.getPosterUrl();
                        bitmap = getBitmap(posterUrl, key);
                    }
                    movieSummary.setPoster_bitmap(bitmap);
                }
            }
        }
        return movieSummaries;
    }

    @Override
    protected void onPostExecute(List<MovieSummary> movieSummaries) {
        super.onPostExecute(movieSummaries);
        response.bitmapsAvailable(movieSummaries);
    }

    protected Bitmap getBitmap(URL url, String key) {
        HttpURLConnection urlConnection = null;
        Bitmap bitmap = null;
        try {
            // Create the request to themoviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        getMemoryCache(bitmap).put(key, bitmap);
        return bitmap;
    }

    protected static LruCache<String, Bitmap> getMemoryCache(Bitmap bitmap) {
        if (bitmap == null) {
            return s_memoryCache;
        }
        if (s_memoryCache == null) {
            int allocateSize = bitmap.getByteCount() * 22;
            s_memoryCache = new LruCache<String,Bitmap>(allocateSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        return s_memoryCache;
    }

}
