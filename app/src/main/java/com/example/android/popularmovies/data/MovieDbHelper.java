package com.example.android.popularmovies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        //Movie Table with all columns
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + MovieContract.Movies.TABLE_NAME + " (" +
                        MovieContract.Movies._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieContract.Movies.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                        MovieContract.Movies.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MovieContract.Movies.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        MovieContract.Movies.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                        MovieContract.Movies.COLUMN_RATING + " REAL NOT NULL, " +
                        MovieContract.Movies.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        MovieContract.Movies.COLUMN_POPULARITY + " REAL NOT NULL, " +
                        MovieContract.Movies.COLUMN_ISFAVORITE + " INTEGER DEFAULT 0, " +
                        MovieContract.Movies.COLUMN_LAST_UPDATED + " INTEGER DEFAULT 0" +
                        " );";

        //Trailers Table with all columns and MovieId foreign key
        final String SQL_CREATE_TRAILER_TABLE =
                "CREATE TABLE " + MovieContract.Trailers.TABLE_NAME + " (" +
                        MovieContract.Trailers._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieContract.Trailers.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                        MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID + " TEXT UNIQUE NOT NULL, " +
                        " FOREIGN KEY (" + MovieContract.Trailers.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MovieContract.Movies.TABLE_NAME + " ("
                        + MovieContract.Movies.COLUMN_MOVIE_ID + ")" +
                        ");";

        //Reviews Table with all columns and MovieId foreign key
        final String SQL_CREATE_REVIEW_TABLE =
                "CREATE TABLE " + MovieContract.Reviews.TABLE_NAME + " (" +
                        MovieContract.Reviews._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        MovieContract.Reviews.COLUMN_REVIEW_ID + " TEXT UNIQUE NOT NULL, " +
                        MovieContract.Reviews.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                        MovieContract.Reviews.COLUMN_REVIEW_AUTHOR + " TEXT NOT NULL, " +
                        MovieContract.Reviews.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL, " +
                        " FOREIGN KEY (" + MovieContract.Reviews.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MovieContract.Movies.TABLE_NAME + " ("
                        + MovieContract.Movies.COLUMN_MOVIE_ID + ")" +
                        ");";

        //Create all tables
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //Wipe the database if the version is upgraded
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.Movies.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.Reviews.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.Trailers.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
