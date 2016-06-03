package com.example.android.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.service.MovieFetchService;

/**
 * Fragment containing grid of movie posters based on the list requested by the user.
 */
public class MainGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    //Constants
    private final String SPINNER_POSITION = "SpinnerPos";

    public static final String[] MOVIE_COLUMNS = {
            MovieContract.Movies.TABLE_NAME + "." + MovieContract.Movies._ID,
            MovieContract.Movies.COLUMN_MOVIE_ID,
            MovieContract.Movies.COLUMN_TITLE,
            MovieContract.Movies.COLUMN_DESCRIPTION,
            MovieContract.Movies.COLUMN_RELEASE_DATE,
            MovieContract.Movies.COLUMN_RATING,
            MovieContract.Movies.COLUMN_POPULARITY,
            MovieContract.Movies.COLUMN_ISFAVORITE,
            MovieContract.Movies.COLUMN_POSTER_PATH,
            MovieContract.Movies.COLUMN_LAST_UPDATED
    };

    //Movie Poster RecyclerView and adapter members
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MovieCursorAdapter mMovieCursorAdapter;

    //Determines which url to use, set through the FLAG constants - top_rated/favorites/popular
    private int SelectedListFlag;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //If the app has not been started before (no saved state)
        if(savedInstanceState == null) {
            //set the default flag to be popular movies
            SelectedListFlag = MovieFetchService.FLAG_POPULAR;
        }
        //Otherwise, restore the previous movies list and the flag that sets the spinner correctly
        else {
            SelectedListFlag = savedInstanceState.getInt(SPINNER_POSITION);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        long lastUpdate = sharedPreferences.getLong("last_updated", -1);

        //Comment out this if statement when debugging the app on empty database so that it can be populated
        //If the database is not up to date (has been tuesday since the last update)
        if(!Utility.isDatabaseUpToDate(lastUpdate)){
            //Update both lists - In this case we are updating a list, so the movie_id parameter is set
            //-1 (invalid id) so the utility class can check for it.
            Utility.updateDB(getContext(), MovieFetchService.FLAG_POPULAR, -1);
            Utility.updateDB(getContext(), MovieFetchService.FLAG_BEST_RATED, -1);

            //Store new timestamp
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("last_updated", System.currentTimeMillis());
            editor.apply();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Save the current state of the spinner and the SelectedListFlag to the bundle to be
        // restored when the app resumes.
        outState.putInt(SPINNER_POSITION, SelectedListFlag);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_main, container, false);
        return mRecyclerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // use this setting to improve performance since changes in content do not change the
        // layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Set the grid layout on the recyclerview that will hold the movie images and the adapter
        mLayoutManager = new GridLayoutManager(getContext(), 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mMovieCursorAdapter =
                new MovieCursorAdapter(getActivity(), null, getActivity().getSupportFragmentManager());
        mRecyclerView.setAdapter(mMovieCursorAdapter);
        mMovieCursorAdapter.notifyDataSetChanged();

        //Update the movie grid with the current selected list
        updateGridFromDB(SelectedListFlag);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        //Setup main activity spinner
        Spinner spinner = (Spinner) menu.findItem(R.id.action_sort).getActionView();
        ArrayAdapter<String>  spinnerAdapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_header, getResources().getStringArray(R.array.sort_options));
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(spinnerAdapter);

        //if activity is being restored, restore the previous SelectedListFlag
        if (spinner.getSelectedItemPosition() != SelectedListFlag) {
            spinner.setSelection(SelectedListFlag);
        }

        //If a different option is selected on the spinner, update the Gridview with the new request.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if the option selected is different than the previous one
                if (SelectedListFlag != position) {
                    SelectedListFlag = position;
                    updateGridFromDB(SelectedListFlag);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * update the UI grid by retrieving the chosen movie list from the database.
     * Destroy any loaders that may still be working when the user changed the selection.
     *
     * @param urlFlag indicates which list has been chosen
     */
    private void updateGridFromDB(int urlFlag) {
        getLoaderManager().destroyLoader(MovieFetchService.FLAG_FAVORITES);
        getLoaderManager().destroyLoader(MovieFetchService.FLAG_POPULAR);
        getLoaderManager().destroyLoader(MovieFetchService.FLAG_BEST_RATED);
        getLoaderManager().initLoader(urlFlag, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            //Load popular movies
            case MovieFetchService.FLAG_POPULAR:
                return new CursorLoader(getActivity(),
                        MovieContract.Movies.POPULAR_URI,
                        MOVIE_COLUMNS,
                        null,
                        null,
                        null); //Sort by descending popularity is set within the provider to avoid errors
            //Load best rated movies
            case MovieFetchService.FLAG_BEST_RATED:
                return new CursorLoader(getActivity(),
                        MovieContract.Movies.BEST_RATED_URI,
                        MOVIE_COLUMNS,
                        null,
                        null,
                        null); //Sort by descending rating is set within the provider to avoid errors
            //Load movies marked as favorite
            case MovieFetchService.FLAG_FAVORITES:
                return new CursorLoader(getActivity(),
                        MovieContract.Movies.FAVORITES_URI,
                        MOVIE_COLUMNS,
                        MovieContract.Movies.COLUMN_ISFAVORITE,
                        new String[] {"1"},
                        null);
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        //Update the adapter with the new data in the cursor and update the view.
        mMovieCursorAdapter.changeCursor(c);
        mMovieCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }
}

