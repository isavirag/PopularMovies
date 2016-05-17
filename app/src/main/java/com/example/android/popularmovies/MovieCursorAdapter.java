package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.adapter.CursorRecyclerViewAdapter;
import com.example.android.popularmovies.data.model.Movie;
import com.example.android.popularmovies.service.MovieFetchService;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Adapter that exposes movie posters from a Cursor to a RecyclerView
 * which will show a grid of movie posters.
 */
public class MovieCursorAdapter extends CursorRecyclerViewAdapter<MovieCursorAdapter.ViewHolder> {

    private Context mContext;
    private FragmentManager mFragmentManager;

    //Constants for Youtube URI creation
    final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    final String SIZE = "w185"; // Recommended image size requested from themoviedb.org

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView mImageView;
        private int mMovieId;
        private Context mContext;
        private FragmentManager mFragmentManager;
        public ViewHolder(ImageView v, Context context, FragmentManager fm) {
            super(v);
            mImageView = v;
            mContext = context;
            mFragmentManager = fm;

            v.setClickable(true);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            //If the device is large and in landscape, use two pane mode by switching the fragment.
            if(Utility.isScreenLargeAndLandscape(mContext)) {
                Fragment movieDetail = new MovieDetailFragment();
                Bundle args = new Bundle();
                args.putInt(MovieFetchService.MOVIE_ID_EXTRA, mMovieId);
                movieDetail.setArguments(args);
                ((MainActivity) mContext).switchContent(movieDetail);
            }
            //Otherwise (small device) or portrait mode on large device: open a movie detail activity
            else{
                Intent movieDetailIntent = new Intent(v.getContext(), MovieDetailActivity.class)
                        .putExtra(MovieFetchService.MOVIE_ID_EXTRA, mMovieId);
                v.getContext().startActivity(movieDetailIntent);
            }
        }

        /**
         * This method is called by the MovieCursorAdapter during OnBindViewHolder to set the
         * MovieId that will be used to load the movie details
         *
         * @param movieId the ID of the movie to be loaded
         */
        public void setMovieId(int movieId) {
            mMovieId = movieId;
        }
    }

    public MovieCursorAdapter(Context context, Cursor cursor, FragmentManager fm){
        super(cursor);
        mContext = context;
        mCursor = cursor;
        //The fragment manager is needed in order to replace the detail fragment in the case
        //of two pane mode (larger devices)
        mFragmentManager = fm;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Poster imageview settings
        ImageView imageView = new ImageView(mContext);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(imageView, mContext, mFragmentManager);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        Movie movie = null;
        try {
            movie = Movie.inflateMovieFromCursor(cursor);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(movie != null) {

            //Pass the movieId to the viewholder - it needs it to create the new fragment
            holder.setMovieId(movie.getMovieId());

            //Build the URI to retrieve the movie poster from the movieDatabase API online
            String posterLoc = movie.getPosterPath();
            Uri builtUri = Uri.parse(POSTER_BASE_URL).buildUpon()
                    .appendEncodedPath(SIZE)
                    .appendEncodedPath(posterLoc)
                    .build();

            //Load images using Picasso library
            Picasso.with(mContext).load(builtUri).into(holder.mImageView);
        }
    }
}
