
package com.example.android.popularmovies.data.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.android.popularmovies.data.MovieContract;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Trailer - the object used to store the JSON data response retrieved from ReviewResponse (through
 * the API). Each review object contains an author and review content.
 */
public class Review {

    @SerializedName("id")
    @Expose
    private String reviewId;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("url")
    @Expose
    private String url;

    /**
     * Get the review ID
     *
     * @return review ID
     */
    public String getReviewId() {
        return reviewId;
    }

    /**
     * Get the Review Author
     *
     * @return Author name
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the Review Author
     *
     * @param author name
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Get the review content
     *
     * @return review content
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the review content
     *
     * @param content review content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Get Url where the review is shown on moviedb.org
     *
     * @return review url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set Url where the review is shown on moviedb.org
     *
     * @param url review url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns a Review object built from the cursor parameter
     *
     * @param c cursor containing the Review data
     * @return A Review object
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static Review inflateFromCursor(Cursor c) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        Review review = new Review();
        review.setAuthor(c.getString(c.getColumnIndex(MovieContract.Reviews.COLUMN_REVIEW_AUTHOR)));
        review.setContent(c.getString(c.getColumnIndex(MovieContract.Reviews.COLUMN_REVIEW_CONTENT)));
        return review;
    }

    /**
     * Returns contentvalues of all relevant fields needed for the review object
     *
     * @return contentvalues of review
     */
    public ContentValues getContentValues(int movieId) {
        ContentValues values = new ContentValues();
        try {
            values.put(MovieContract.Reviews.COLUMN_MOVIE_ID, movieId);
            values.put(MovieContract.Reviews.COLUMN_REVIEW_ID, getReviewId());
            values.put(MovieContract.Reviews.COLUMN_REVIEW_AUTHOR, getAuthor());
            values.put(MovieContract.Reviews.COLUMN_REVIEW_CONTENT, getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }
}
