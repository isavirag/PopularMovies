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
package com.example.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Movie Provider class that serves as a ContentProvider for the PopularMovies app.
 */
public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    // URI Matcher IDs
    static final int MOVIES = 100;
    static final int MOVIE = 101;
    static final int POPULAR_MOVIES = 102;
    static final int BEST_RATED_MOVIES = 103;
    static final int FAVORITE_MOVIES = 104;
    static final int TRAILERS = 200;
    static final int REVIEWS = 300;

    // Static QueryBuilders
    private static final SQLiteQueryBuilder sTrailers;
    private static final SQLiteQueryBuilder sReviews;

    /**
     * Builds the UriMatcher for all URI requests possibilities
     *
     * @return the matcher containing all possible URIs
     */
    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // content://authority/movies
        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES);

        // content://authority/movies/[movieId]
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", MOVIE);

        // content://authority/movies/popular
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/"
                + MovieContract.PATH_POPULAR, POPULAR_MOVIES);

        // content://authority/movies/bestrated
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/"
                + MovieContract.PATH_BEST_RATED, BEST_RATED_MOVIES);

        // content://authority/movies/favorites
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/"
                + MovieContract.PATH_FAVORITES, FAVORITE_MOVIES);

        // content://authority/movies/[movieId]/trailers
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#/" +
                MovieContract.PATH_TRAILERS, TRAILERS);

        // content://authority/movies/[movieId]/reviews
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#/" +
                MovieContract.PATH_REVIEWS, REVIEWS);
        return matcher;
    }

    // Static block to create the QueryBuilders (Inner Joins for the trailers and reviews table)
    static {
        sTrailers = new SQLiteQueryBuilder();
        sTrailers.setTables(
                MovieContract.Movies.TABLE_NAME + " INNER JOIN " +
                        MovieContract.Trailers.TABLE_NAME +
                        " ON " + MovieContract.Movies.TABLE_NAME +
                        "." + MovieContract.Movies.COLUMN_MOVIE_ID +
                        " = " + MovieContract.Trailers.TABLE_NAME +
                        "." + MovieContract.Trailers.COLUMN_MOVIE_ID);

        sReviews = new SQLiteQueryBuilder();
        sReviews.setTables(
                MovieContract.Movies.TABLE_NAME + " INNER JOIN " +
                        MovieContract.Reviews.TABLE_NAME +
                        " ON " + MovieContract.Movies.TABLE_NAME +
                        "." + MovieContract.Movies.COLUMN_MOVIE_ID +
                        " = " + MovieContract.Reviews.TABLE_NAME +
                        "." + MovieContract.Reviews.COLUMN_MOVIE_ID);
    }

    @Override
    public boolean onCreate() {
        //Open a database to be used by the provider
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is. In the case of Popular movies
        // and best rated movies, the type is the movies content_type since they don't
        // have their own tables, but rather are subset lists of the main movies table
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return MovieContract.Movies.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.Movies.CONTENT_ITEM_TYPE;
            case POPULAR_MOVIES:
                return MovieContract.Movies.CONTENT_TYPE;
            case BEST_RATED_MOVIES:
                return MovieContract.Movies.CONTENT_TYPE;
            case FAVORITE_MOVIES:
                return MovieContract.Movies.CONTENT_TYPE;
            case TRAILERS:
                return MovieContract.Trailers.CONTENT_TYPE;
            case REVIEWS:
                return MovieContract.Reviews.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;

        List<String> uriSegments;
        String movieId;

        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        switch (sUriMatcher.match(uri)) {
            // "movie/#"
            case MOVIE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.Movies.TABLE_NAME,
                        projection,
                        MovieContract.Movies.COLUMN_MOVIE_ID + "= ?",
                        new String[] {uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder
                );
                break;
            // "popular" - sort by is set to descending popularity
            case POPULAR_MOVIES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.Movies.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        MovieContract.Movies.COLUMN_POPULARITY + " DESC LIMIT 20"
                );
                break;
            // "bestrated" - sort by is set to descending rating
            case BEST_RATED_MOVIES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.Movies.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        MovieContract.Movies.COLUMN_RATING + " DESC LIMIT 20"
                );
                break;
            // "favorites"
            case FAVORITE_MOVIES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.Movies.TABLE_NAME,
                        projection,
                        MovieContract.Movies.COLUMN_ISFAVORITE + "= ?",
                        new String[] {"1"},
                        null,
                        null,
                        sortOrder + " LIMIT 20"
                );
                break;
            //
            case TRAILERS:
                //the 2nd to last path segment in the URI is the movie ID to get its corresponding trailers
                uriSegments = uri.getPathSegments();
                movieId = uri.getPathSegments().get(uriSegments.size() - 2);

                retCursor = sTrailers.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        MovieContract.Trailers.TABLE_NAME + "." + MovieContract.Movies.COLUMN_MOVIE_ID + "= ?",
                        new String[] {movieId},
                        null,
                        null,
                        sortOrder
                );
                break;
            case REVIEWS:
                //the 2nd to last path segment in the URI is the movie ID to get its corresponding reviews
                uriSegments = uri.getPathSegments();
                movieId = uri.getPathSegments().get(uriSegments.size() - 2);
                retCursor = sReviews.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        MovieContract.Reviews.TABLE_NAME + "." + MovieContract.Movies.COLUMN_MOVIE_ID + "= ?",
                        new String[] {movieId},
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //watch for changes that happen to the URI (tells the UI when the cursor changes).
        Context context = getContext();
        if (context != null) {
            retCursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        long _id;

        switch (match) {
            case MOVIE:
                _id = db.insert(MovieContract.Movies.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieContract.Movies
                            .getUriForMovie(values
                                    .getAsLong(MovieContract.Movies.COLUMN_MOVIE_ID));
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case TRAILERS:
                _id = db.insert(MovieContract.Trailers.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieContract.Trailers
                            .getUriForTrailers(values
                                    .getAsLong(MovieContract.Trailers.COLUMN_MOVIE_ID));
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case REVIEWS:
                _id = db.insert(MovieContract.Reviews.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MovieContract.Reviews
                            .getUriForReviews(values
                                    .getAsLong(MovieContract.Reviews.COLUMN_MOVIE_ID));
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // We do not have a need to delete items from the database at this moment
        throw new UnsupportedOperationException("Deleting items is currently not supported");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match){
            case MOVIE: {
                rowsUpdated = db.update(MovieContract.Movies.TABLE_NAME,
                        values,
                        MovieContract.Movies.COLUMN_MOVIE_ID + "= ?",
                        new String[] {uri.getLastPathSegment()});
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported uri: " + uri);
            }

        }
        if (rowsUpdated != 0) {
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Context context = getContext();
        int returnCount = 0;

        switch (match) {
            //Bulk insert a list of movies (This works for all: popular, best-rated and favorites
            // since they all are stored in one table)
            case MOVIES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        //If the database doesn't contain this movie
                        if(!isDataAlreadyInDB(MovieContract.Movies.TABLE_NAME, MovieContract.Movies.COLUMN_MOVIE_ID,
                                Integer.toString(value.getAsInteger(MovieContract.Movies.COLUMN_MOVIE_ID)))){
                            long _id = db.insert(MovieContract.Movies.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                        //Otherwise, the movie is already in the database - update popularity and rating
                        // values in the database case they have changed
                        else{
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(MovieContract.Movies.COLUMN_POPULARITY,
                                    value.getAsInteger(MovieContract.Movies.COLUMN_POPULARITY));
                            contentValues.put(MovieContract.Movies.COLUMN_RATING,
                                    value.getAsInteger(MovieContract.Movies.COLUMN_RATING));

                            db.update(MovieContract.Movies.TABLE_NAME,
                                    contentValues,
                                    MovieContract.Movies.COLUMN_MOVIE_ID + "= ?",
                                    new String[] {Integer.toString(
                                            value.getAsInteger(MovieContract.Movies.COLUMN_MOVIE_ID))});
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (context != null) {
                    context.getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            //Bulk insert a list of trailers
            case TRAILERS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        //Add trailers to the db only if they are not already on there for this movie
                        if(!isDataAlreadyInDB(MovieContract.Trailers.TABLE_NAME, MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID,
                                value.getAsString(MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID))){
                            long _id = db.insert(MovieContract.Trailers.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (context != null) {
                    context.getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            //Bulk insert a list of reviews
            case REVIEWS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        //Add reviews to the db only if they are not already on there for this movie
                        if(!isDataAlreadyInDB(MovieContract.Reviews.TABLE_NAME, MovieContract.Reviews.COLUMN_REVIEW_ID,
                                value.getAsString(MovieContract.Reviews.COLUMN_REVIEW_ID))){
                            long _id = db.insert(MovieContract.Reviews.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                if (context != null) {
                    context.getContentResolver().notifyChange(uri, null);
                }
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    /**
     * Check if a particular movie, review or trailer is already stored in the database
     *
     * @param TableName the table where the field is set
     * @param dbfield the field that you want to check the value for
     * @param fieldValue the value you want to check if it's already stored
     *
     * @return true if the value is already in the database, false otherwise.
     */
    private boolean isDataAlreadyInDB(String TableName,
                                            String dbfield, String fieldValue) {

        Cursor cursor = mOpenHelper.getReadableDatabase().query(
                TableName,
                null,
                dbfield + "= ?",
                new String[] {fieldValue},
                null,
                null,
                null
        );

        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // method specifically to assist the testing framework in running smoothly.
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}