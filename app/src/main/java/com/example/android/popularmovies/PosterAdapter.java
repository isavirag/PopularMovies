package com.example.android.popularmovies;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PosterAdapter extends BaseAdapter {

    private ArrayList<Movie> movies = new ArrayList<Movie>();
    private Context mContext;

    public PosterAdapter(Activity context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Movie getItem(int position) {
        return movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Return the imageView for one specific location in the gridView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        String posterLoc = getItem(position).getPosterImagePath();
        final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
        final String SIZE = "w185"; // Recommended size requested from the Movie Database API

        Uri builtUri = Uri.parse(POSTER_BASE_URL).buildUpon()
                .appendEncodedPath(SIZE)
                .appendEncodedPath(posterLoc)
                .build();

        // if it's not recycled already, initialize the Imageview attributes
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        //Load images using Picasso library
        Picasso.with(mContext).load(builtUri).into(imageView);

        return imageView;
    }

    //Clear all items from the adapter
    public void clear() {
        movies.clear();
    }

    //Add a new movie to the adapter
    public void add(Movie movie){
        movies.add(movie);
    }

    //Add a list of movies to the adapter
    public void addAll(ArrayList<Movie> movieList){
        movies.addAll(movieList);
    }

}
