
package com.example.android.popularmovies.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * TrailerResponse is the JSON data response we retrieve from www.themoviedb.org after a GET request
 * for a specific movie. The results contain the list of trailers.
 */
public class TrailerResponse {

    //members
    @SerializedName("id")
    @Expose
    private int movieId;
    @SerializedName("results")
    @Expose
    private List<Trailer> mTrailers = new ArrayList<>();

    /**
     * Get the movie id
     *
     * @return the movie id
     */
    public int getMovieId() {
        return movieId;
    }

    /**
     * Set the movie id
     *
     * @param id the movie id
     */
    public void setMovieId(int id) {
        this.movieId = id;
    }

    /**
     * Get the list of trailers retrieved
     *
     * @return the list of trailers retrieved
     */
    public List<Trailer> getTrailers() {
        return mTrailers;
    }

}
