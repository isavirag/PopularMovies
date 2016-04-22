package com.example.android.popularmovies;


import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Movie implements Parcelable {


    private String mTitle;
    private String mPosterImagePath;
    private String mDescription;
    private double mRating;
    private String mReleaseDate;

    public Movie(){
    }

    protected Movie(Parcel in) {
        mTitle = in.readString();
        mPosterImagePath = in.readString();
        mDescription = in.readString();
        mRating = in.readDouble();
        mReleaseDate = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getPosterImagePath() {
        return mPosterImagePath;
    }

    public void setPosterImagePath(String posterImagePath) {
        mPosterImagePath = posterImagePath;
    }

    public double getRating() {
        return mRating;
    }

    public void setRating(double rating) {
        mRating = rating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {

        //Use SimpleDateFormat to split all three numbers and show the month in text form.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date convertedDate;
        String year = "";
        int monthNumber;
        String monthWord = "";
        String day = "";

        try {
            convertedDate = dateFormat.parse(releaseDate);
            SimpleDateFormat dYear = new SimpleDateFormat("yyyy", Locale.US);
            year = dYear.format(convertedDate);
            SimpleDateFormat dMonth = new SimpleDateFormat("MM", Locale.US);
            monthNumber = Integer.parseInt(dMonth.format(convertedDate));
            monthWord  = new DateFormatSymbols().getMonths()[monthNumber-1];
            SimpleDateFormat dDay = new SimpleDateFormat("dd", Locale.US);
            day = dDay.format(convertedDate);

                    ;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mReleaseDate = monthWord + " " + year;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mPosterImagePath);
        dest.writeString(mDescription);
        dest.writeDouble(mRating);
        dest.writeString(mReleaseDate);
    }
}