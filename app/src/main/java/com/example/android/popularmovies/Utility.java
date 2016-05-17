package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ParseException;

import com.example.android.popularmovies.service.MovieFetchService;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A helper utility class that includes general static methods used within the Popular Movies App.
 */
public class Utility {

    /**
     * Update the database by starting a MovieFetchService with the corresponding flag and movie_id
     * when applicable. Reviews, trailers and movie need to specify a movie id. For lists, only the
     * flag is necessary.
     *
     * @param context the context of the calling activity/fragment
     * @param flag Indicates the type of data to be updated. For example - trailers_flag
     * @param movie_id Indicates the movie_id f updating reviews or trailers for a specific one.
     */
    static public void updateDB(Context context, int flag, int movie_id) {
        Intent intent = new Intent(context, MovieFetchService.class);

        intent.putExtra(MovieFetchService.MOVIE_QUERY_TYPE_EXTRA, flag);
        if(movie_id != -1){
            intent.putExtra(MovieFetchService.MOVIE_ID_EXTRA, movie_id);
        }
        context.startService(intent);
    }

    /**
     * Combines the isScreenSizeLarge method and the isLandscape method to check if a device
     * is both large and currently in landscape mode
     *
     * @param context the context of the calling activity/fragment
     * @return true if the device is large and in landscape, false otherwise.
     */
    public static boolean isScreenLargeAndLandscape(Context context){
        return isScreenSizeLarge(context) && isLandscape(context);
    }

    /**
     * Checks if the device used is a large device
     *
     * @param context the context of the calling activity/fragment
     * @return true if the device is large, false otherwise
     */
    private static boolean isScreenSizeLarge(Context context){
        int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE);
    }

    /**
     * Returns true if the screen is currently set in landscape mode
     *
     * @param context the context of the calling activity/fragment
     * @return true if screen is in landscape mode, false otherwise
     */
    private static boolean isLandscape(Context context){
        return (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * Returns the Formatted Date in Simple text form. Example: August 1920
     *
     * @param releaseDate release date of the movie in default form. Example: 19-08-1920
     * @return the simplified formatted Date
     */
    public static String formatDate(String releaseDate) {

        //Use SimpleDateFormat to split all three numbers and show the month in text form.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date convertedDate;
        String year = "";
        int monthNumber;
        String monthWord = "";

        try {
            convertedDate = dateFormat.parse(releaseDate);
            SimpleDateFormat dYear = new SimpleDateFormat("yyyy", Locale.US);
            year = dYear.format(convertedDate);
            SimpleDateFormat dMonth = new SimpleDateFormat("MM", Locale.US);
            monthNumber = Integer.parseInt(dMonth.format(convertedDate));
            monthWord  = new DateFormatSymbols().getMonths()[monthNumber-1];
            SimpleDateFormat dDay = new SimpleDateFormat("dd", Locale.US);

        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        return monthWord + " " + year;
    }

    /**
     * Checks if it is time to update the lists database or the trailers and reviews on a given
     * movie based on the lastUpdated parameter passed in - MovieDB updates their lists every Tuesday.
     *
     * @param context context of the fragment that is calling the method.
     * @return true if the Database needs to be updated, false otherwise.
     */
    public static boolean isDatabaseUpToDate(Context context, long lastUpdated) {

        long currentTimeStamp = System.currentTimeMillis();
        Date curDateTime = new Date(currentTimeStamp);
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDateTime);

        long nextTuesdayAfterUpdate = nextTuesdayAfterUpdate(lastUpdated);

        if (currentTimeStamp > lastUpdated && currentTimeStamp > nextTuesdayAfterUpdate) {
            //Database needs to be updated
            return false;
        }
        //Database is up to date
        return true;
    }

    /**
     * Returns the date indicating when the next available update from MovieDB happened or will
     * happen which is the following tuesday after the last update to the app database.
     * MovieDB online updates on Tuesdays.
     *
     * @param lastUpdated the date of the last update made to the app database.
     * @return the date of the next tuesday after the last update made to the database.
     */
    private static long nextTuesdayAfterUpdate(long lastUpdated){
        Date updateDateTime = new Date(lastUpdated);
        Calendar lastUpdateCal = Calendar.getInstance();
        lastUpdateCal.setTime(updateDateTime);

        int weekday = lastUpdateCal.get(Calendar.DAY_OF_WEEK);

        if (weekday != Calendar.TUESDAY)
        {
            //To get the days away from last update, subtract the day from the last day of the week
            //{saturday), then add the difference to the target date(Tuesday).Saturday to Tuesday = 3.
            //Then use mod 7(total week days) to get the days away from the last update and add it on.
            int daysAway = (Calendar.SATURDAY - weekday + 3) % 7;
            lastUpdateCal.add(Calendar.DAY_OF_YEAR, daysAway);
        }
        else{
            //If it was a tuesday, add a whole week
            lastUpdateCal.add(Calendar.DAY_OF_YEAR, 7);
        }
        return lastUpdateCal.getTimeInMillis();
    }
}
