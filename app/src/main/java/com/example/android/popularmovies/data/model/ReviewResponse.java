
package com.example.android.popularmovies.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewResponse is the JSON data response we retrieve from www.themoviedb.org after a GET request
 * for a specific movie. Results contains the list of reviews.
 */
public class ReviewResponse {

    @SerializedName("id")
    @Expose
    private int movieId;
    @SerializedName("page")
    @Expose
    private int page;
    @SerializedName("results")
    @Expose
    private List<Review> mReviews = new ArrayList<Review>();
    @SerializedName("total_pages")
    @Expose
    private int totalPages;
    @SerializedName("total_results")
    @Expose
    private int totalResults;

    /**
     * Get a list of reviews for a specific movie
     *
     * @return list of reviews
     */
    public List<Review> getReviews() {
        return mReviews;
    }

    /**
     * Get Movie Id
     *
     * @return Movie ID
     */
    public int getMovieId() {
        return movieId;
    }

    /**
     * get Page retrieved
     *
     * @return the current page number
     */
    public int getPage() {
        return page;
    }

    /**
     * The total pages containing all reviews
     *
     * @return total pages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * The amount of reviews available for this movie
     *
     * @return amount of total reviews
     */
    public int getTotalResults() {
        return totalResults;
    }

}
