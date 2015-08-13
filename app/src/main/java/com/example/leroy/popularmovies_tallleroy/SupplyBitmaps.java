package com.example.leroy.popularmovies_tallleroy;

import android.content.Context;
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
    protected static final Object s_cacheLock = new Object();
    protected static Context context = null;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 100; // 100MB
    private static final String DISK_CACHE_SUBDIR = "posters";

    public SupplyBitmaps(AsyncSupplyBitmapResponse response, Context context) {
        this.response = response;
        this.context = context;
    }

    public static Bitmap getLocalBitmap(String key) {
        Bitmap bitmap = null;
        synchronized (s_cacheLock) {
            // try the memory cache first
            LruCache<String, Bitmap> memoryCache = getMemoryCache(null);
            if (memoryCache != null) {
                bitmap = memoryCache.get(key);
            }
            if (bitmap == null) {
                bitmap = Utility.getBitmapFromFile(key, context);
                if (bitmap != null) {
                    memoryCache = getMemoryCache(bitmap);
                    memoryCache.put(key, bitmap);
                }
            }
        }
        return bitmap;
    }

    @Override
    protected List<MovieSummary> doInBackground(List<MovieSummary>... params) {
        List<MovieSummary> movieSummaries = params[0];
        for(MovieSummary movieSummary : movieSummaries) {
            if (movieSummary.getPosterBitmap() == null) {
                synchronized (s_cacheLock) {
                    String key = movieSummary.getPosterKey();
                    Bitmap bitmap = null;
                    // try the memory cache first
                    LruCache<String, Bitmap> memoryCache = getMemoryCache(null);
                    if (memoryCache != null) {
                        bitmap = memoryCache.get(key);
                    }
                    if (bitmap == null) {
                        bitmap = Utility.getBitmapFromFile(key, context);
                        if (bitmap != null) {
                            // put the image into our memory cache
                            memoryCache = getMemoryCache(bitmap);
                            memoryCache.put(key, bitmap);
                        }
                    }
                    // finally go on out to themoviedb and update the memory and disk cache
                    if (bitmap == null) {
                        URL posterUrl = movieSummary.getPosterUrl();
                        bitmap = getBitmap(posterUrl, key);
                        if (bitmap != null) {
                            // put the image into our memory cache
                            memoryCache = getMemoryCache(bitmap);
                            memoryCache.put(key, bitmap);
                            // and put it in the local file system
                            Utility.saveBitmapToFile(key, bitmap, context);
                        }
                    }
                    movieSummary.setHasLocalPosterBitmap(true);
                    // free up the memory
                    bitmap = null;
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return bitmap;
    }

    public static LruCache<String, Bitmap> getMemoryCache(Bitmap bitmap) {
        if (bitmap == null) {
            return s_memoryCache;
        }
        if (s_memoryCache == null) {
            int allocateSize = bitmap.getByteCount() * 20;
            // this size will be about 80 meg for the largest posters
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
