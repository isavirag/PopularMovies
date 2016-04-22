package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
        final String SIZE = "w185";

        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        //Get the intent that was sent to be able to retrieve the extras (movie information)
        Intent intent = getActivity().getIntent();

        //Display all of the movie details on the movie detail activity
        if(intent != null && intent.hasExtra("Movie")){

            Movie movie = intent.getParcelableExtra("Movie");

            //Set all of the textviews with the new information
            ((TextView) rootView.findViewById(R.id.movie_title)).setText(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.movie_description)).setText(movie.getDescription());
            ((TextView) rootView.findViewById(R.id.movie_rating)).setText(getString(R.string.movie_rating_fraction, movie.getRating()));
            ((TextView) rootView.findViewById(R.id.movie_release_date)).setText(movie.getReleaseDate());

            ImageView imageView2 = (ImageView) rootView.findViewById(R.id.movie_poster);
            String posterLoc = movie.getPosterImagePath();

            Uri builtUri = Uri.parse(POSTER_BASE_URL).buildUpon()
                    .appendEncodedPath(SIZE)
                    .appendEncodedPath(posterLoc)
                    .build();

            imageView2.setAdjustViewBounds(true);
            imageView2.setScaleType(ImageView.ScaleType.CENTER_CROP);

            //Set image for Movie Poster using Picasso
            Picasso.with(getContext()).load(builtUri).into(imageView2);

        }
        return rootView;
    }
}
