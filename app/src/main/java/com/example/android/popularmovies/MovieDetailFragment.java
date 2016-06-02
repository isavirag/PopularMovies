package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.service.MovieFetchService;
import com.squareup.picasso.Picasso;

/**
 * Movie Detail Fragment containing the movie detail images, description, trailers and reviews.
 */
public class MovieDetailFragment extends Fragment
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    //Constants
    private final int FLAG_MOVIE = 6;
    private final String SIZE = "w185";
    private final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    final String YOUTUBE_VID_BASE_URL = "http://www.youtube.com/watch";

    //Trailers View and adapter members
    private RecyclerView mTrailerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private TrailerCursorAdapter mTrailerCursorAdapter;

    //Reviews View and adapter members
    private RecyclerView mReviewsView;
    private RecyclerView.LayoutManager mReviewLayoutManager;
    private ReviewCursorAdapter mReviewCursorAdapter;

    //Members
    private int movieId;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private TextView mRatingTextView;
    private TextView mReleaseDateTextView;
    private ImageView mPosterImageView;
    private Button mFavoriteButton;
    private ShareActionProvider mShareActionProvider;

    public MovieDetailFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        //Initialize the xml views
        mDescriptionTextView = (TextView) view.findViewById(R.id.movie_description);
        mRatingTextView = (TextView) view.findViewById(R.id.movie_rating);
        mReleaseDateTextView = (TextView) view.findViewById(R.id.movie_release_date);
        mPosterImageView = (ImageView) view.findViewById(R.id.movie_poster);

        //Setup the trailers layout and adapter
        mTrailerView = (RecyclerView) view.findViewById(R.id.trailer_recycler_view);
        mTrailerView.setNestedScrollingEnabled(false);
        mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        mTrailerView.setLayoutManager(mLayoutManager);
        mTrailerCursorAdapter = new TrailerCursorAdapter(getActivity(), null);
        mTrailerView.setAdapter(mTrailerCursorAdapter);

        //Setup the reviews layout and adapter
        mReviewsView = (RecyclerView) view.findViewById(R.id.review_recycler_view);
        mReviewsView.setNestedScrollingEnabled(false);
        mReviewLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
        mReviewsView.setLayoutManager(mReviewLayoutManager);
        mReviewCursorAdapter = new ReviewCursorAdapter(getActivity(), null);
        mReviewsView.setAdapter(mReviewCursorAdapter);

        //Get the intent that was sent from activity (on small devices only) to be able to retrieve
        // the extras (movie id)
        Intent intent = getActivity().getIntent();
        movieId = -1;
        if(intent != null && intent.hasExtra(MovieFetchService.MOVIE_ID_EXTRA)){
            movieId = intent.getIntExtra(MovieFetchService.MOVIE_ID_EXTRA, -1);
        }
        //Otherwise, check if the movie_id is passed through a bundle with the FragmentManager
        //This happens in two-pane mode on larger devices in landscape mode
        else if (this.getArguments() != null && Utility.isScreenLargeAndLandscape(getContext())){
            movieId = this.getArguments().getInt(MovieFetchService.MOVIE_ID_EXTRA, -1);
            mFavoriteButton = (Button) view.findViewById((R.id.favorite_button));
            mFavoriteButton.setOnClickListener(this);
            mTitleTextView = (TextView) view.findViewById((R.id.title_textview));
        }

        //If movie id is valid, update the database if necessary and initialize loaders.
        if(movieId != -1){
            if(!isMovieDetailsUpdated(movieId)){
                Utility.updateDB(getContext(), MovieFetchService.FLAG_TRAILERS, movieId);
                Utility.updateDB(getContext(), MovieFetchService.FLAG_REVIEWS, movieId);
            }
            getLoaderManager().initLoader(MovieFetchService.FLAG_TRAILERS, null, this);
            getLoaderManager().initLoader(MovieFetchService.FLAG_REVIEWS, null, this);
            getLoaderManager().initLoader(FLAG_MOVIE, null, this);
        }
    }

    /**
     * Check if the movie details are up to date. Movie trailers and reviews are only ever retrieved
     * from the online API when a user clicks on it from the grid. Therefore, it will not always be
     * up to date when the main lists are updated.
     *
     * @param movieId the movie id we are checking to see if it's up to date.
     * @return true if it is up to date, false otherwise.
     */
    private boolean isMovieDetailsUpdated(int movieId) {

        //Make a query to the DB to check when the movie details were last updated
        Uri movieUri = MovieContract.Movies.getUriForMovie(movieId);
        Cursor c = getContext().getContentResolver().query(movieUri,
                new String[] {MovieContract.Movies.COLUMN_LAST_UPDATED},
                MovieContract.Movies.COLUMN_MOVIE_ID, new String[] {Integer.toString(movieId)}, null);

        if(c!=null && c.moveToFirst()) {
            long lastUpdated = c.getLong(c.getColumnIndex(MovieContract.Movies.COLUMN_LAST_UPDATED));
            long currentTimeStamp = System.currentTimeMillis();

            //If the movie has been fetched before (lastupdated =0) and it hasn't been tuesday since,
            //close the cursor and return true - the movie details are up to date.
            if (lastUpdated != 0 && Utility.isDatabaseUpToDate(getContext(), lastUpdated)) {
                c.close();
                return true;
            }
            //Otherwise, update the last_updated value for the movie in the database and return false
            // this will trigger the reviews and trailers to be updated where this method was called.
            else{
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.Movies.COLUMN_LAST_UPDATED, currentTimeStamp);
                getContext().getContentResolver().update(MovieContract.Movies
                        .getUriForMovie(movieId), contentValues, null, null);
                c.close();
                return false;
            }
        }
        return false;
    }

    public String getFirstTrailer(){

        Uri trailerUri = MovieContract.Trailers.getUriForTrailers(movieId);
        Cursor c = getContext().getContentResolver().query(trailerUri,
                new String[] {MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID},
                MovieContract.Trailers.COLUMN_MOVIE_ID, new String[] {Integer.toString(movieId)}, null);

        if(c!=null && c.moveToFirst()) {
            return c.getString(c.getColumnIndex(MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID));
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        Boolean isFavorite = isFavoriteCheck();

        //If the image is clicked and it was not a favorite before:
        if(!isFavorite){
            //set the image to be "favorite"
            mFavoriteButton.setBackgroundResource(R.drawable.star_button_yellow2);
            //update db
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieContract.Movies.COLUMN_ISFAVORITE, 1);
            getContext().getContentResolver().update(MovieContract.Movies.getUriForMovie(movieId), contentValues, null, null);
        }
        //Otherwise, if the image is clicked and it was a favorite:
        else{
            //revert the image to not be "favorite"
            mFavoriteButton.setBackgroundResource(R.drawable.plus_button_grey);
            //update db
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieContract.Movies.COLUMN_ISFAVORITE, 0);
            getContext().getContentResolver().update(MovieContract.Movies.getUriForMovie(movieId), contentValues, null, null);
        }
        //Notify the change to the favorites URI in order for the favorites list grid to be updated.
        getContext().getContentResolver().notifyChange(MovieContract.Movies.FAVORITES_URI, null);
    }

    /**
     * Check if the current movie is checked as favorite in the local database
     *
     * @return true if it is a Favorite, false otherwise
     */
    private Boolean isFavoriteCheck() {
        //Build the Uri for the movie and query the movies table with the favorite column as projection.
        Uri movieUri = MovieContract.Movies.getUriForMovie(movieId);
        Cursor c = getContext().getContentResolver().query(movieUri,
                new String[] {MovieContract.Movies.COLUMN_ISFAVORITE},
                null,
                null,
                null);

        //return true (favorite) if the value is 1, false otherwise
        if(c!=null && c.moveToFirst()) {
            int isFavorite = c.getInt(c.getColumnIndex(MovieContract.Movies.COLUMN_ISFAVORITE));
            if (isFavorite == 1) {
                c.close();
                return true;
            }
            else{
                c.close();
                return false;
            }
        }
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //If fragment is showing on a small device (movie_detail_activity is using the fragment, not
        // the main activity in two pane mode), set the favorite button located on the activity's
        // actionbar. This can only be done after the Activity is created.
        if(getFragmentManager().findFragmentByTag(MainActivity.MOVIEDETFRAGTAG) == null) {
            mFavoriteButton = ((MovieDetailActivity) getActivity()).getButton();
            Boolean isFavorite = isFavoriteCheck();
            if (isFavorite) {
                mFavoriteButton.setBackgroundResource(R.drawable.star_button_yellow2);
            }
            mFavoriteButton.setOnClickListener(this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.movie_detail_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (movieId != -1 ) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private Intent createShareTrailerIntent() {

        Uri youtubeUri;

        if(getFirstTrailer() != null) {

            String youtubeId = getFirstTrailer();
            //Get first movie trailer link
            youtubeUri = Uri.parse(YOUTUBE_VID_BASE_URL).buildUpon()
                    .appendQueryParameter("v", youtubeId)
                    .build();
            Log.d("TEST", "createShareTrailerIntent: IM here " + youtubeUri);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    youtubeUri.toString());
            return shareIntent;
        }
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            //load the general details of the movie
            case FLAG_MOVIE:
                return new CursorLoader(getActivity(),
                        MovieContract.Movies.getUriForMovie(movieId),
                        MainGridFragment.MOVIE_COLUMNS,
                        null,
                        null,
                        null);
            //load trailers for current movie
            case MovieFetchService.FLAG_TRAILERS:
                return new CursorLoader(getActivity(),
                        MovieContract.Trailers.getUriForTrailers(movieId),
                        MovieContract.Trailers.TRAILER_PROJECTION,
                        null,
                        null,
                        null);
            //load reviews for current movie
            case MovieFetchService.FLAG_REVIEWS:
                return new CursorLoader(getActivity(),
                        MovieContract.Reviews.getUriForReviews(movieId),
                        MovieContract.Reviews.REVIEW_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case FLAG_MOVIE:
                if(data.moveToFirst()) {
                    String title = data.getString(data.getColumnIndex(MovieContract.Movies.COLUMN_TITLE));

                    //If we are in two-pane layout mode, set the title text inside the fragment
                    if(getFragmentManager().findFragmentByTag(MainActivity.MOVIEDETFRAGTAG) != null){
                        mTitleTextView.setText(title);
                    }
                    //Otherwise (the fragment is shown in its own activity on a small device), set the title on
                    //the action bar.
                    else{
                        ((MovieDetailActivity) getActivity()).setActionBarTitle(title);
                    }
                    //Set all layout views corresponding to the data in the cursor
                    mDescriptionTextView.setText(data.getString(data.getColumnIndex(MovieContract.Movies.COLUMN_DESCRIPTION)));
                    mRatingTextView.setText(getString(R.string.movie_rating_fraction,
                            Double.parseDouble(data.getString(data.getColumnIndex(MovieContract.Movies.COLUMN_RATING)))));
                    mReleaseDateTextView.setText(Utility.formatDate(
                            data.getString(data.getColumnIndex(MovieContract.Movies.COLUMN_RELEASE_DATE))));

                    //Set the favorite indicator image
                    boolean isFavorite = (data.getInt(data.getColumnIndex(MovieContract.Movies.COLUMN_ISFAVORITE)))== 1;
                    mFavoriteButton.setBackgroundResource(isFavorite ? R.drawable.star_button_yellow2: R.drawable.plus_button_grey);

                    //Build the poster  URI and retrieve it using Picasso
                    String posterLoc = data.getString(data.getColumnIndex(MovieContract.Movies.COLUMN_POSTER_PATH));
                    Uri builtUri = Uri.parse(POSTER_BASE_URL).buildUpon()
                            .appendEncodedPath(SIZE)
                            .appendEncodedPath(posterLoc)
                            .build();
                    mPosterImageView.setAdjustViewBounds(true);
                    mPosterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Picasso.with(getContext()).load(builtUri).into(mPosterImageView);

                    // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareTrailerIntent());
                    }
                }
                break;
            case MovieFetchService.FLAG_TRAILERS:
                //Replace the null cursor with the new one that contains the trailers data and notify
                //the adapter so that the new list is visible.
                mTrailerCursorAdapter.changeCursor(data);
                mTrailerCursorAdapter.notifyDataSetChanged();
                break;
            case MovieFetchService.FLAG_REVIEWS:
                //Replace the null cursor with the new one that contains the reviews data and notify
                //the adapter so that the new list is visible.
                mReviewCursorAdapter.changeCursor(data);
                mReviewCursorAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {}
}