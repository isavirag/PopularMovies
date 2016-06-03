package com.example.android.popularmovies;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.service.MovieFetchService;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

/**
 * Movie Detail Fragment containing the movie detail images, description, trailers and reviews.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int TOKEN_UPDATE_LASTUPDATED = 1;
    public static final int TOKEN_UPDATE_FAVORITE = 2;

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
    private MenuItem mFavoriteButton;
    private Boolean mIsFavorite;
    private String mFirstTrailer;

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
            mTitleTextView = (TextView) view.findViewById((R.id.title_textview));
        }

        //If movie id is valid, update the database if necessary and initialize loaders.
        if(movieId != -1){
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
     * @param lastUpdated Time when the movie details (reviews and trailers) were last updated.
     */
    private boolean isMovieDetailsUpdated(long lastUpdated) {

            long currentTimeStamp = System.currentTimeMillis();

            //If the movie has been fetched before (lastupdated =0) and it hasn't been tuesday since,
            //close the cursor and return true - the movie details are up to date.
            if (lastUpdated != 0 && Utility.isDatabaseUpToDate(lastUpdated)) {
                return true;
            }
            //Otherwise, update the last_updated value for the movie in the database and return false
            // this will trigger the reviews and trailers to be updated where this method was called.
            else{
                //Update the "last Updated" field asynchronously through an AsyncQueryHandler
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.Movies.COLUMN_LAST_UPDATED, currentTimeStamp);
                MovieQueryHandler queryHandlerUpdateTime = new MovieQueryHandler(getContext(), getContext().getContentResolver());
                queryHandlerUpdateTime.startUpdate(
                    TOKEN_UPDATE_LASTUPDATED,
                    null,
                    MovieContract.Movies.getUriForMovie(movieId),
                    contentValues,
                    null,
                    null
                    );
                return false;
            }
        }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.movie_detail_fragment, menu);

        // Retrieve the favorite menu item and set it to the right image
        mFavoriteButton = menu.findItem(R.id.action_favorite);

        if(mFavoriteButton != null && mIsFavorite != null) {
            mFavoriteButton.setIcon(mIsFavorite ? R.drawable.star_button_yellow2 : R.drawable.plus_button_grey);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        int favorite;

        switch (id) {
            case R.id.action_favorite:
                //If the image is clicked and it was not a favorite before:
                if(mIsFavorite != null) {
                    if (!mIsFavorite) {
                        //set the image to be "favorite"
                        mFavoriteButton.setIcon(R.drawable.star_button_yellow2);
                        getActivity().invalidateOptionsMenu();
                        favorite = 1;
                    }
                    //Otherwise, if the image is clicked and it was a favorite:
                    else {
                        //revert the image to not be "favorite"
                        mFavoriteButton.setIcon(R.drawable.plus_button_grey);
                        getActivity().invalidateOptionsMenu();
                        favorite = 0;
                    }
                    //Update the Favorite field asynchronously through an AsyncQueryHandler
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MovieContract.Movies.COLUMN_ISFAVORITE, favorite);
                    MovieQueryHandler queryHandlerUpdateFav = new MovieQueryHandler(
                            getContext(), getContext().getContentResolver());
                    queryHandlerUpdateFav.startUpdate(
                            TOKEN_UPDATE_FAVORITE,
                            null,
                            MovieContract.Movies.getUriForMovie(movieId),
                            contentValues,
                            null,
                            null
                    );
                }
                return true;
            case R.id.action_share:
                createShareTrailerIntent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates an Intent to share the first movie trailer youtube link with any app
     * installed on the device that will accept sharing
     */
    private void createShareTrailerIntent() {

        Uri youtubeUri;

        if(mFirstTrailer != null) {
            youtubeUri = Uri.parse(YOUTUBE_VID_BASE_URL).buildUpon()
                    .appendQueryParameter("v", mFirstTrailer)
                    .build();
            Log.d("TEST", "createShareTrailerIntent: IM here " + youtubeUri);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    youtubeUri.toString());
            startActivity(shareIntent);
        }
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
                    mIsFavorite = (data.getInt(data.getColumnIndex(MovieContract.Movies.COLUMN_ISFAVORITE)))== 1;

                    if(mFavoriteButton != null) {
                        mFavoriteButton.setIcon(mIsFavorite ? R.drawable.star_button_yellow2 : R.drawable.plus_button_grey);
                    }
                    getActivity().invalidateOptionsMenu();

                    long lastUpdated = data.getLong(data.getColumnIndex(MovieContract.Movies.COLUMN_LAST_UPDATED));

                    if(!isMovieDetailsUpdated(lastUpdated)) {
                        Utility.updateDB(getContext(), MovieFetchService.FLAG_TRAILERS, movieId);
                        Utility.updateDB(getContext(), MovieFetchService.FLAG_REVIEWS, movieId);
                    }

                    //Build the poster  URI and retrieve it using Picasso
                    String posterLoc = data.getString(data.getColumnIndex(MovieContract.Movies.COLUMN_POSTER_PATH));
                    Uri builtUri = Uri.parse(POSTER_BASE_URL).buildUpon()
                            .appendEncodedPath(SIZE)
                            .appendEncodedPath(posterLoc)
                            .build();
                    mPosterImageView.setAdjustViewBounds(true);
                    mPosterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Picasso.with(getContext()).load(builtUri).into(mPosterImageView);
                }
                break;
            case MovieFetchService.FLAG_TRAILERS:
                //Replace the null cursor with the new one that contains the trailers data and notify
                //the adapter so that the new list is visible.
                mTrailerCursorAdapter.changeCursor(data);
                if(data.moveToFirst()) {
                    mFirstTrailer = (data.getString(data.getColumnIndex(MovieContract.Trailers.COLUMN_YOUTUBE_TRAILER_ID)));
                }
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

    /**
     * MovieQueryHandler handles any needed database queries that are not handled through the Content
     * Provider URIs
     */
    public static class MovieQueryHandler extends AsyncQueryHandler {

        WeakReference<Context> mContext;

        public MovieQueryHandler(Context context, ContentResolver cr) {
            super(cr);
            mContext = new WeakReference<>(context);
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            switch(token) {
                //Make sure the Content Resolver is notified of the change after the favorite
                // field is updated sdo that the favorites list automatically updated.
                case MovieDetailFragment.TOKEN_UPDATE_FAVORITE:
                    Context context = mContext.get();
                    if (context != null) {
                        context.getContentResolver().notifyChange(MovieContract.Movies.FAVORITES_URI, null);
                    }
                    break;
            }
        }
    }
}