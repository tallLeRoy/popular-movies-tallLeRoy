/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.leroy.popularmovies_tallleroy.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PostersProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PostersDbHelper mOpenHelper;

    static final int POSTERS = 100;
    static final int POSTER_BY_MOVIE_ID = 101;
    static final int TRAILERS = 102;
    static final int TRAILERS_BY_MOVIE_ID = 103;
    static final int REVIEWS = 104;
    static final int REVIEWS_BY_MOVIE_ID = 105;

    static UriMatcher buildUriMatcher() {
         // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PostersContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, PostersContract.PATH_POSTERS, POSTERS);
        matcher.addURI(authority, PostersContract.PATH_POSTERS + "/*", POSTER_BY_MOVIE_ID);
        matcher.addURI(authority, PostersContract.PATH_TRAILERS, TRAILERS);
        matcher.addURI(authority, PostersContract.PATH_TRAILERS + "/*", TRAILERS_BY_MOVIE_ID);
        matcher.addURI(authority, PostersContract.PATH_REVIEWS, REVIEWS);
        matcher.addURI(authority, PostersContract.PATH_REVIEWS + "/*", REVIEWS_BY_MOVIE_ID);

        return matcher;
    }

   @Override
    public boolean onCreate() {
        mOpenHelper = new PostersDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case TRAILERS:
            case TRAILERS_BY_MOVIE_ID:
                return PostersContract.TrailersEntry.CONTENT_TYPE;
            case REVIEWS:
            case REVIEWS_BY_MOVIE_ID:
                return PostersContract.ReviewsEntry.CONTENT_TYPE;
            case POSTER_BY_MOVIE_ID:
                return PostersContract.PostersEntry.CONTENT_ITEM_TYPE;
            case POSTERS:
                return PostersContract.PostersEntry.CONTENT_TYPE;
             default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "trailers"
            case TRAILERS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PostersContract.TrailersEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "trailers/*"
            case TRAILERS_BY_MOVIE_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PostersContract.TrailersEntry.TABLE_NAME,
                        projection,
                        PostersContract.TrailersEntry.TRAILERS_BY_MOVIE_ID_SELECTION,
                        new String[] { PostersContract.TrailersEntry.getMovieIdFromUri(uri) },
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "reviews"
            case REVIEWS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PostersContract.ReviewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "reviews/*"
            case REVIEWS_BY_MOVIE_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PostersContract.ReviewsEntry.TABLE_NAME,
                        projection,
                        PostersContract.ReviewsEntry.REVIEWS_BY_MOVIE_ID_SELECTION,
                        new String[] { PostersContract.ReviewsEntry.getMovieIdFromUri(uri) },
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "posters/*"
            case POSTER_BY_MOVIE_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PostersContract.PostersEntry.TABLE_NAME,
                        projection,
                        PostersContract.PostersEntry.POSTER_BY_MOVIE_ID_SELECTION,
                        new String[] { PostersContract.PostersEntry.getMovieIdFromUri(uri) },
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "posters"
            case POSTERS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        PostersContract.PostersEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TRAILERS: {
                long _id = db.insert(PostersContract.TrailersEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PostersContract.TrailersEntry.buildTrailersUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long _id = db.insert(PostersContract.ReviewsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PostersContract.ReviewsEntry.buildReviewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case POSTERS: {
                long _id = db.insert(PostersContract.PostersEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PostersContract.PostersEntry.buildPosterUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case TRAILERS:
                rowsDeleted = db.delete(
                        PostersContract.TrailersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                rowsDeleted = db.delete(
                        PostersContract.ReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case POSTERS:
                rowsDeleted = db.delete(
                        PostersContract.PostersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case TRAILERS:
                rowsUpdated = db.update(PostersContract.TrailersEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated = db.update(PostersContract.ReviewsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case POSTERS:
                rowsUpdated = db.update(PostersContract.PostersEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case TRAILERS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PostersContract.TrailersEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case REVIEWS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PostersContract.ReviewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case POSTERS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                      long _id = db.insert(PostersContract.PostersEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

   @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

    public static void wipeout(Context context) {
        PostersDbHelper dbHelper = new PostersDbHelper(context);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 0, 1);
    }
}