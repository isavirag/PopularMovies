package com.example.android.popularmovies.service;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.data.model.Movie;
import com.example.android.popularmovies.data.model.MovieListResponse;
import com.example.android.popularmovies.data.model.Review;
import com.example.android.popularmovies.data.model.ReviewResponse;
import com.example.android.popularmovies.data.model.Trailer;
import com.example.android.popularmovies.data.model.TrailerResponse;
import com.example.android.popularmovies.data.remote.MoviesFactory;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import retrofit2.Response;

/**
 * MovieFetchService is a service that fetches data from www.themoviedb.org
 * It can fetch a list of popular movies, a list of best rated movies or
 * lists of trailers or reviews for a specific movie
 */
public class MovieFetchService extends IntentService {

    //Constants
    private final String LOG_TAG = MovieFetchService.class.getSimpleName();
    public static final String MOVIE_QUERY_TYPE_EXTRA = "query";
    public static final String MOVIE_ID_EXTRA = "id";
    public static final int FLAG_POPULAR = 0;
    public static final int FLAG_BEST_RATED = 1;
    public static final int FLAG_FAVORITES = 2;
    public static final int FLAG_TRAILERS = 3;
    public static final int FLAG_REVIEWS = 4;

    //members
    private List<Movie> mMovies;
    private List<Trailer> mTrailers;
    private List<Review> mReviews;

    public MovieFetchService(){
        super("MovieFetch");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Retrieve the flag sent through the intent extra (defaults to -1)
        int flag = intent.getIntExtra(MOVIE_QUERY_TYPE_EXTRA, -1);
        int movieId = -1;

        //Initialize a new factory for the Retrofit library to handle the HTTP call
        MoviesFactory factory = new MoviesFactory();

        // Make all HTTP requests synchronously since we are in a worker thread
        // For retrofit, .execute() is synchronous and .enqueue() is asynchronous
        switch (flag) {
            case FLAG_BEST_RATED:
                try {
                    Response<MovieListResponse> response = factory.getInstance().topRatedMovies(BuildConfig.OPEN_MOVIE_DB_API_KEY).execute();
                    mMovies = response.body().getMovies();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "onFailure: " + e.getMessage());
                }
                break;
            case FLAG_POPULAR:
                try {
                    Response<MovieListResponse> response = factory.getInstance().popularMovies(BuildConfig.OPEN_MOVIE_DB_API_KEY).execute();
                    mMovies = response.body().getMovies();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "onFailure: " + e.getMessage());
                }
                break;
            case FLAG_TRAILERS:
                movieId = intent.getIntExtra(MOVIE_ID_EXTRA, -1);
                try {
                    Response<TrailerResponse> response = factory.getInstance().movieTrailers(movieId, BuildConfig.OPEN_MOVIE_DB_API_KEY).execute();
                    mTrailers = response.body().getTrailers();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "onFailure: " + e.getMessage());
                }
                break;
            case FLAG_REVIEWS:
                movieId = intent.getIntExtra(MOVIE_ID_EXTRA, -1);
                try {
                    Response<ReviewResponse> response = factory.getInstance().movieReviews(movieId, BuildConfig.OPEN_MOVIE_DB_API_KEY).execute();
                    mReviews = response.body().getReviews();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        //If there are movies to insert, store Content Values for each on a vector to convert to an
        // array and finally bulkInsert the array into the database
        if (mMovies != null && mMovies.size() > 0) {
            Vector<ContentValues> cVVector = new Vector<>(mMovies.size());
            for (int i = 0; i < mMovies.size() ; i++) {
                cVVector.add(mMovies.get(i).getContentValues());
            }
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            this.getContentResolver().bulkInsert(MovieContract.Movies.CONTENT_URI, cvArray);

            Log.d(LOG_TAG, "Movie Service Complete. " + cVVector.size() + " Inserted");
        }

        //If there are trailers to insert, store Content Values for each on a vector to convert to an
        // array and finally bulkInsert the array into the database
        if (mTrailers != null && mTrailers.size() > 0){
            Vector<ContentValues> cVVector = new Vector<>(mTrailers.size());
            for (int i = 0; i < mTrailers.size() ; i++) {
                cVVector.add(mTrailers.get(i).getContentValues(movieId));
            }
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            this.getContentResolver().bulkInsert(MovieContract.Trailers.getUriForTrailers(movieId), cvArray);

        }

        //If there are movies to insert, store Content Values for each on a vector to convert to an
        // array and finally bulkInsert the array into the database
        if (mReviews != null && mReviews.size() > 0) {
            Vector<ContentValues> cVVector = new Vector<>(mReviews.size());
            for (int i = 0; i < mReviews.size() ; i++) {
                cVVector.add(mReviews.get(i).getContentValues(movieId));
            }
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            this.getContentResolver().bulkInsert(MovieContract.Reviews.getUriForReviews(movieId), cvArray);
        }
    }
}
