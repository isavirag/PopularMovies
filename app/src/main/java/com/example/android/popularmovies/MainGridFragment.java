package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainGridFragment extends Fragment {


    //Flags indicating the two possible base URLs depending on what the user requests.
    private final int FLAG_POPULAR = 0;
    private final int FLAG_RATING = 1;

    //Determines which url to use as a base - top_rated or popular, set through the FLAG constants
    private int urlFlag;

    //Main list of movies that will be stored whenever an API call is made and its adapter
    private ArrayList<Movie> mMovies;
    private PosterAdapter mPosterAdapter;

    private final String LOG_TAG = MainGridFragment.class.getSimpleName();

    public MainGridFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        //If the app has not been started before (no saved state)
        if(savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            mMovies = new ArrayList<>();
            urlFlag = FLAG_POPULAR;
            updateGrid();
        }
        //Otherwise, restore the previous movies list and the flag that sets the spinner to the correct option.
        else {
            mMovies = savedInstanceState.getParcelableArrayList("movies");
            urlFlag = savedInstanceState.getInt("SpinnerPosition");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Save the current state of the list and the urlFlag to the bundle to be restored when the app resumes.
        outState.putParcelableArrayList("movies", mMovies);
        outState.putInt("SpinnerPosition", urlFlag);
        super.onSaveInstanceState(outState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        //Setup of the main gridview (and adapter) that shows all movies
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        mPosterAdapter = new PosterAdapter(getActivity());
        gridview.setAdapter(mPosterAdapter);

        //restore all previous movies if they were previously requested and saved when app stopped.
        if(mMovies !=null) {
            mPosterAdapter.addAll(mMovies);
            mPosterAdapter.notifyDataSetChanged();
        }

        //Start detail activity when any movie poster is clicked - add the corresponding movie object to the intent.
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent movieDetailIntent = new Intent(getActivity(), MovieDetailActivity.class).putExtra("Movie", mMovies.get(position));
                startActivity(movieDetailIntent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        //Setup main activity spinner
        Spinner spinner = (Spinner) menu.findItem(R.id.action_sort).getActionView();
        ArrayAdapter<String>  spinnerAdapter = new ArrayAdapter<>(getContext(),R.layout.spinner_header, getResources().getStringArray(R.array.sort_options));
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(spinnerAdapter);

        //if activity is being restored, restore the previous urlFlag
        if (spinner.getSelectedItemPosition() != urlFlag) {
            spinner.setSelection(urlFlag);
        }

        //If a different option is selected on the spinner, update the Gridview with the new request.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if the option selected is different than the previous one
                if (urlFlag != position) {

                    //clear the current movie list and set the flag to the new position
                    mMovies.clear();
                    urlFlag = position;

                    //update the gridLayout - making a new API call
                    updateGrid();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //update movie trailer images when app is started or sorting criteria is changed
    private void updateGrid(){
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute();
    }

    //Make an API call to theMovieDatabase through an AsyncTask
    private class FetchMoviesTask extends AsyncTask<Void, Void, ArrayList<Movie>> {

        //GET request to theMovieDatabase
        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //Constants stating the two possible base URL depending on what the user requests.
            final String BASE_URL_RATING = "http://api.themoviedb.org/3/movie/top_rated?";
            final String BASE_URL_POPULAR = "http://api.themoviedb.org/3/movie/popular?";
            final String APPID_PARAM = "api_key";

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            String currentBaseURL;

            //Set the correct base url by checking the flag
            try {
                if(urlFlag == FLAG_POPULAR){
                    currentBaseURL = BASE_URL_POPULAR;
                }
                else if (urlFlag == FLAG_RATING){
                    currentBaseURL = BASE_URL_RATING;
                } else {
                    throw new IllegalArgumentException("A known URL flag must be set");
                }

                //Build the URL with base url and api key
                Uri builtUri = Uri.parse(currentBaseURL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                //Save response
                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                //Pass in the new data to get saved into movie objects.
                return getMovieDataFromJson(moviesJsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            //this will only happen if there was an error getting or parsing the response
            return null;
        }


        //Take in the JSON response and save the relevant fields into movie objects
        private ArrayList<Movie> getMovieDataFromJson(String moviesJsonStr) throws JSONException {

            // Tags of the JSON objects that need to be extracted from the JSON response.
            final String TMD_LIST = "results";
            final String TMD_TITLE = "original_title";
            final String TMD_POSTER = "poster_path";
            final String TMD_DESCRIPTION = "overview";
            final String TMD_RATING = "vote_average";
            final String TMD_RELEASE_DATE = "release_date";

            //Declare and store all of the movies returned by the JSON GET request
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMD_LIST);

            for(int i = 0; i < moviesArray.length(); i++) {

                JSONObject movieDetail = moviesArray.getJSONObject(i);
                String title = movieDetail.getString(TMD_TITLE);
                String description = movieDetail.getString(TMD_DESCRIPTION);
                double rating = movieDetail.getDouble(TMD_RATING);
                String releaseDate = movieDetail.getString(TMD_RELEASE_DATE);
                String posterLocation = movieDetail.getString(TMD_POSTER);

                //Save every movie into a movie object
                Movie movie = new Movie();
                movie.setTitle(title);
                movie.setDescription(description);
                movie.setRating(rating);
                movie.setReleaseDate(releaseDate);
                movie.setPosterImagePath(posterLocation);
                mMovies.add(movie);
            }

            //return the list of movies
            return mMovies;
        }



        @Override
        protected void onPostExecute(ArrayList<Movie> result) {

            if (result != null) {
                //Clear the adapter and add all of the new movies so that they can be shown in the layout
                mPosterAdapter.clear();
                mPosterAdapter.addAll(result);
                mPosterAdapter.notifyDataSetChanged();
            }
        }
    }


}
