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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.leroy.popularmovies_tallleroy.data.PostersContract.*;

/**
 * Manages a local database for themoviedb poster data.
 */
public class PostersDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 13;

    static final String DATABASE_NAME = "posters.db";

    public PostersDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_POSTERS_TABLE = "CREATE TABLE " + PostersContract.PostersEntry.TABLE_NAME + " (" +
                PostersEntry._ID + " INTEGER PRIMARY KEY, " +
                PostersEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE NOT NULL, " +
                PostersEntry.COLUMN_ADULT + " TEXT, " +
                PostersEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
                PostersEntry.COLUMN_FAVORITE + " TEXT, " +
                PostersEntry.COLUMN_GENRE_IDS + " TEXT, " +
                PostersEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT, " +
                PostersEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
                PostersEntry.COLUMN_OVERVIEW + " TEXT, " +
                PostersEntry.COLUMN_POPULARITY + " REAL, " +
                PostersEntry.COLUMN_POSTER_PATH + " TEXT, " +
                PostersEntry.COLUMN_RELEASE_DATE + " TEXT, " +
                PostersEntry.COLUMN_RUNTIME + " TEXT, " +
                PostersEntry.COLUMN_TITLE + " TEXT, " +
                PostersEntry.COLUMN_VIDEO + " TEXT, " +
                PostersEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                PostersEntry.COLUMN_VOTE_COUNT + " INTEGER, " +
                PostersEntry.COLUMN_INSERT_DATE + " DATE DEFAULT CURRENT_DATE " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_POSTERS_TABLE);

        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + PostersContract.TrailersEntry.TABLE_NAME + " (" +
                PostersContract.TrailersEntry._ID + " INTEGER PRIMARY KEY, " +
                PostersContract.TrailersEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                PostersContract.TrailersEntry.COLUMN_QUICKTIME + " INTEGER, " +
                PostersContract.TrailersEntry.COLUMN_YOUTUBE + " INTEGER, " +
                PostersContract.TrailersEntry.COLUMN_TITLE + " TEXT, " +
                PostersContract.TrailersEntry.COLUMN_SIZE + " TEXT, " +
                PostersContract.TrailersEntry.COLUMN_SOURCE + " TEXT UNIQUE NOT NULL, " +
                PostersContract.TrailersEntry.COLUMN_TYPE + " TEXT " +
                PostersContract.TrailersEntry.COLUMN_INSERT_DATE + " DATE DEFAULT CURRENT_DATE " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_TRAILERS_TABLE);


        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + PostersContract.ReviewsEntry.TABLE_NAME + " (" +
                PostersContract.ReviewsEntry._ID + " INTEGER PRIMARY KEY, " +
                PostersContract.ReviewsEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                PostersContract.ReviewsEntry.COLUMN_REVIEW_ID + " TEXT, " +
                PostersContract.ReviewsEntry.COLUMN_AUTHOR + " TEXT, " +
                PostersContract.ReviewsEntry.COLUMN_CONTENT + " TEXT, " +
                PostersContract.ReviewsEntry.COLUMN_URL + " TEXT UNIQUE NOT NULL," +
                PostersContract.ReviewsEntry.COLUMN_INSERT_DATE + " DATE DEFAULT CURRENT_DATE " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PostersEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PostersContract.TrailersEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PostersContract.ReviewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
