package com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The Movie Contract that sets up the constants used by the database and content provider
 * to keep it all consistent.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";

    // CONTENT_AUTHORITY creates the base of all URIs which apps will use to
    // contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Uri Paths
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_BEST_RATED = "bestrated";
    public static final String PATH_POPULAR = "popular";
    public static final String PATH_FAVORITES = "favorites";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";

    /* Inner class that defines the table contents of the Movies table */
    public static final class Movies implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();

        public static final Uri BEST_RATED_URI = CONTENT_URI.buildUpon()
                .appendPath(PATH_BEST_RATED)
                .build();

        public static final Uri POPULAR_URI = CONTENT_URI.buildUpon()
                .appendPath(PATH_POPULAR)
                .build();

        public static final Uri FAVORITES_URI = CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES)
                .build();

        // Content types
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        // Table name
        public static final String TABLE_NAME = "movies";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_DESCRIPTION = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_RATING = "vote_average";
        public static final String COLUMN_ISFAVORITE = "favorite";
        public static final String COLUMN_LAST_UPDATED = "lastupdate";

        /**
         * Helper method to get the full URI for a specific movie
         *
         * @param movieId {@link int}
         * @return {@link Uri}
         */
        public static Uri getUriForMovie(long movieId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieId);
        }
    }

    /* Inner class that defines the table contents of the Trailers table */
    public static final class Trailers implements BaseColumns {

        // Content type for getting trailers
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        // Table name
        public static final String TABLE_NAME = "trailers";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_YOUTUBE_TRAILER_ID = "youtube_trailer_id";

        //The projection used in the database requests include the tablename on both the .id and the
        //movie_id in order to remove ambiguity created by duplicate columns after the inner join.
        public static final String[] TRAILER_PROJECTION = {
                MovieContract.Trailers.TABLE_NAME + "." + MovieContract.Trailers._ID,
                MovieContract.Trailers.TABLE_NAME + "." + MovieContract.Trailers.COLUMN_MOVIE_ID,
                MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID
        };


        /**
         * Get the URI of all the trailers for a specific movie
         *
         * Example: content://authority/movies/[movieId]/trailers
         * @param movieId the movie id needed to retrieve its trailers
         * @return {@link Uri}
         */
        public static Uri getUriForTrailers(long movieId) {
            return Movies.getUriForMovie(movieId).buildUpon()
                    .appendPath(PATH_TRAILERS)
                    .build();
        }
    }

    /* Inner class that defines the table contents of the Reviews table */
    public static final class Reviews implements BaseColumns {

        // Content type for getting reviews
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        // Table name
        public static final String TABLE_NAME = "reviews";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_REVIEW_AUTHOR = "author";
        public static final String COLUMN_REVIEW_CONTENT = "review";

        //The projection used in the database requests include the tablename on both the .id and the
        //movie_id in order to remove ambiguity created by duplicate columns after the inner join.
        public static final String[] REVIEW_PROJECTION = {
                MovieContract.Reviews.TABLE_NAME + "." + MovieContract.Reviews._ID,
                MovieContract.Reviews.TABLE_NAME + "." + MovieContract.Reviews.COLUMN_MOVIE_ID,
                MovieContract.Reviews.TABLE_NAME + "." + MovieContract.Reviews.COLUMN_REVIEW_ID,
                MovieContract.Reviews.COLUMN_REVIEW_AUTHOR,
                MovieContract.Reviews.COLUMN_REVIEW_CONTENT
        };

        /**
         * Get the URI of all the reviews for a specific movie
         *
         * * Example: content://authority/movies/[movieId]/reviews
         * @param movieId the movie id needed to retrieve its reviews
         * @return {@link Uri}
         */
        public static Uri getUriForReviews(long movieId) {
            return Movies.getUriForMovie(movieId).buildUpon().appendPath(PATH_REVIEWS).build();
        }
    }

}
