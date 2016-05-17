
package com.example.android.popularmovies.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * MovieListResponse is the JSON data response we retrieve from www.themoviedb.org after a GET request
 * for a list of movies - popular or best_rated. Results contains the list of movies.
 */
public class MovieListResponse {

    //members
    @SerializedName("page")
    @Expose
    private int page;
    @SerializedName("results")
    @Expose
    private List<Movie> mMovies = new ArrayList<Movie>();
    @SerializedName("total_results")
    @Expose
    private int totalResults;
    @SerializedName("total_pages")
    @Expose
    private int totalPages;

    /**
     * Get the list of Movies retrieved
     *
     * @return list of movies
     */
    public List<Movie> getMovies() {
        return mMovies;
    }


    /**
     * Get the current page number of the results
     *
     * @return page number
     */
    public int getPage() {
        return page;
    }

    /**
     * Get total amount of movies available
     *
     * @return total movies
     */
    public int getTotalResults() {
        return totalResults;
    }

    /**
     * * Get total amount of pages available
     *
     * @return total pages
     */
    public int getTotalPages() {
        return totalPages;
    }
}
