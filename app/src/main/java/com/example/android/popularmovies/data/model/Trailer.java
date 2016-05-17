package com.example.android.popularmovies.data.model;


import android.content.ContentValues;
import android.database.Cursor;

import com.example.android.popularmovies.data.MovieContract;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Trailer - the object used to store the JSON data response retrieved from TrailerResponse (through
 * the API). Each trailer object contains a youtube video id (key) to retrieve the video.
 */
public class Trailer {

    //member variables
    private String movieId;
    @SerializedName("id")
    @Expose
    private String trailerId;
    @SerializedName("key")
    @Expose
    private String youtubeId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("site")
    @Expose
    private String site;
    @SerializedName("size")
    @Expose
    private int size;
    @SerializedName("type")
    @Expose
    private String type;

    /**
     * Get the movie Id
     *
     * @return movieId
     */
    public String getMovieId() {
        return movieId;
    }

    /**
     * Set the movie Id
     * Note: the movie ID is not part of the JSON response- it is part of the request
     *
     * @param movieId to be set in the object
     */
    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    /**
     * Get the trailer Id
     *
     * @return movieId
     */
    public String getTrailerId() {
        return trailerId;
    }

    /**
     * Set the trailer Id
     *
     * @param trailerId to be set in the object
     */
    public void setTrailerId(String trailerId) {
        this.trailerId = trailerId;
    }

    /**
     * Get the Youtube ID for the trailer
     *
     * @return the Youtube ID for the trailer
     */
    public String getYoutubeId() {
        return youtubeId;
    }

    /**
     * Get the Youtube ID for the trailer
     *
     * @param youtubeId to be set in the object
     */
    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    /**
     * Get the name of the trailer
     *
     * @return name of the trailer
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of the trailer
     *
     * @param name name of the trailer
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a Trailer object built from the cursor parameter
     *
     * @param c cursor containing trailer data
     * @return Trailer object
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static Trailer inflateTrailerFromCursor(Cursor c) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        Trailer trailer = new Trailer();
        trailer.setMovieId(c.getString(c.getColumnIndex(MovieContract.Trailers.COLUMN_MOVIE_ID)));
        trailer.setYoutubeId(c.getString(c.getColumnIndex(MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID)));
        return trailer;
    }

    /**
     * Returns contentvalues of all relevant fields needed for the trailer object
     *
     * @return contentvalues of trailer
     */
    public ContentValues getContentValues(int movieID) {
        ContentValues values = new ContentValues();
        try {
            values.put(MovieContract.Trailers.COLUMN_MOVIE_ID, movieID);
            values.put(MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID, getYoutubeId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * Returns website where the trailer video is from (currently all from Youtube) - not in use
     * @return trailer video website
     */
    public String getSite() {
        return site;
    }

    /**
     * Returns video resolution - not currently in use
     * example: 1080
     *
     * @return video resolution
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the type of video - not currently in use
     *
     * Example: trailer
     * @return type of video
     */
    public String getType() {
        return type;
    }
}