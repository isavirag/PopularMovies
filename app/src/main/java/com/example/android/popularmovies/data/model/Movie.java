
package com.example.android.popularmovies.data.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.android.popularmovies.data.MovieContract;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Movie - the object used to store the JSON data response retrieved from MovieListResponse(through
 * the API). Each movie object contains a large set of fields described below.
 */
public class Movie {

    @SerializedName("poster_path")
    @Expose
    private String posterPath;
    @SerializedName("overview")
    @Expose
    private String overview;
    @SerializedName("release_date")
    @Expose
    private String releaseDate;
    @SerializedName("genre_ids")
    @Expose
    private List<Integer> genreIds = new ArrayList<>();
    @SerializedName("id")
    @Expose
    private int movieId;
    @SerializedName("original_title")
    @Expose
    private String originalTitle;
    @SerializedName("original_language")
    @Expose
    private String originalLanguage;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("backdrop_path")
    @Expose
    private String backdropPath;
    @SerializedName("popularity")
    @Expose
    private double popularity;
    @SerializedName("vote_count")
    @Expose
    private int voteCount;
    @SerializedName("vote_average")
    @Expose
    private double Rating;
    private int isFavorite;

    public Movie() {}

    /**
     * Get the path within moviedb.org that holds the movie poster
     *
     * @return poster path where the image is stored
     */
    public String getPosterPath() {
        return posterPath;
    }

    /**
     * Set the path within moviedb.org that holds the movie poster
     *
     * @param posterPath path where the image is stored
     */
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    /**
     * Get the description of the movie
     *
     * @return Movie excerpt
     */
    public String getOverview() {
        return overview;
    }

    /**
     * Set the description of the movie
     *
     * @param overview Movie Excerpt
     */
    public void setOverview(String overview) {
        this.overview = overview;
    }

    /**
     * Get the date when the movie was released
     *
     * @return Release date of the movie
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Set the date when the movie was released
     *
     * @param releaseDate Release date of the movie
     */
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * Get the movie Id (taken from www.moviedb.org)
     *
     * @return movie Id
     */
    public int getMovieId() {
        return movieId;
    }

    /**
     * Set the movie Id (taken from www.moviedb.org)
     *
     * @param movieId movie Id
     */
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    /**
     * get the Movie title
     *
     * @return the Movie title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the movie title
     *
     * @param title the movie title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the popularity rating for the movie
     *
     * @return popularity rating
     */
    public double getPopularity() {
        return popularity;
    }

    /**
     * Set the popularity rating for the movie
     *
     * @param popularity rating
     */
    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    /**
     * Get the movie general rating
     *
     * @return movie rating
     */
    public double getRating() {
        return Rating;
    }

    /**
     * Set the movie general rating
     *
     * @param rating movie rating to be set
     */
    public void setRating(double rating) {
        this.Rating = rating;
    }

    /**
     * Check if the movie is a Favorite
     *
     * @return 1 if the movie is a favorite, 0 otherwise
     */
    public int getIsFavorite() {
        return isFavorite;
    }

    /**
     * Set the movie as favorite or not favorite
     *
     * @param isFavorite 1 if the movie is a favorite, 0 otherwise
     */
    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }

    /**
     * Returns a Movie object built from the cursor parameter
     *
     * @param c cursor containing the Movie data
     * @return A Movie object
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static Movie inflateMovieFromCursor(Cursor c) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Movie movie = new Movie();
        movie.setMovieId(c.getInt(c.getColumnIndex(MovieContract.Movies.COLUMN_MOVIE_ID)));
        movie.setTitle(c.getString(c.getColumnIndex(MovieContract.Movies.COLUMN_TITLE)));
        movie.setOverview(c.getString(c.getColumnIndex(MovieContract.Movies.COLUMN_DESCRIPTION)));
        movie.setReleaseDate(c.getString(c.getColumnIndex(MovieContract.Movies.COLUMN_RELEASE_DATE)));
        movie.setRating(c.getDouble(c.getColumnIndex(MovieContract.Movies.COLUMN_RATING)));
        movie.setPopularity(c.getDouble(c.getColumnIndex(MovieContract.Movies.COLUMN_POPULARITY)));
        movie.setIsFavorite(c.getInt(c.getColumnIndex(MovieContract.Movies.COLUMN_ISFAVORITE)));
        movie.setPosterPath(c.getString(c.getColumnIndex(MovieContract.Movies.COLUMN_POSTER_PATH)));
        return movie;
    }

    /**
     * Returns contentvalues of all relevant fields needed for the movie object
     *
     * @return contentvalues of movie
     */
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        try {
            values.put(MovieContract.Movies.COLUMN_MOVIE_ID, getMovieId());
            values.put(MovieContract.Movies.COLUMN_TITLE, getTitle());
            values.put(MovieContract.Movies.COLUMN_DESCRIPTION, getOverview());
            values.put(MovieContract.Movies.COLUMN_RELEASE_DATE, getReleaseDate());
            values.put(MovieContract.Movies.COLUMN_RATING, getRating());
            values.put(MovieContract.Movies.COLUMN_POPULARITY, getPopularity());
            values.put(MovieContract.Movies.COLUMN_ISFAVORITE, getIsFavorite());
            values.put(MovieContract.Movies.COLUMN_POSTER_PATH, getPosterPath());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }


    /**
     * Get the original movie title
     *
     * @return original movie title
     */
    public String getOriginalTitle() {
        return originalTitle;
    }

    /**
     * Set the original movie title
     *
     * @param originalTitle the original movie title
     */
    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    /**
     * Get the original language of the movie
     *
     * @return original language
     */
    public String getOriginalLanguage() {
        return originalLanguage;
    }

    /**
     * Set the original language of the movie
     *
     * @param originalLanguage the language of the movie
     */
    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    /**
     * Get the backdrop path for the movie (image)
     *
     * @return where the backdrop is stored (in moviedb.org)
     */
    public String getBackdropPath() {
        return backdropPath;
    }

    /**
     * Set the backdrop path for the movie
     *
     * @param backdropPath where the backdrop is stored (in moviedb.org)
     */
    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    /**
     * get Amount of reviews that contribute to movie rating
     *
     * @return vote count
     */
    public int getVoteCount() {
        return voteCount;
    }

    /**
     * Set Amount of reviews that contribute to movie rating
     *
     * @param voteCount - in moviedb.org
     */
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    /**
     * Get list of genre Ids for movie (int)
     * @return list of genre IDs
     */
    public List<Integer> getGenreIds() {
        return genreIds;
    }

    /**
     * Set list of genre IDs for movie
     *
     * @param genreIds - in moviedb.org
     */
    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }
}
