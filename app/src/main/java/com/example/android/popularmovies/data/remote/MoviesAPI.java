package com.example.android.popularmovies.data.remote;

import com.example.android.popularmovies.data.model.MovieListResponse;
import com.example.android.popularmovies.data.model.ReviewResponse;
import com.example.android.popularmovies.data.model.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Movies API interface used with Retrofit to set up all of the different possible http calls
 * (in the MovieDB.org API paths) within the popular movies app.
 * Currently only using GET calls to retrieve all information of movies, trailers and reviews.
 */
public interface MoviesApi {

    @GET("movie/top_rated")
    Call<MovieListResponse> topRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/popular")
    Call<MovieListResponse> popularMovies(@Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<TrailerResponse> movieTrailers(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewResponse> movieReviews(@Path("id") int id, @Query("api_key") String apiKey);

}


