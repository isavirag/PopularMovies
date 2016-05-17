package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

/**
 * Movie Detail Activity - holds the MovieDetailFragment on phone application.
 */
public class MovieDetailActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    /**
     * Returns the button that is used to mark the movie as favorite/not favorite
     * This needs to be passed into the detail fragment since the button is part of the actionbar
     *
     * @return The button indicating the movie as favorite/not favorite
     */
    public Button getButton() {
        return (Button) findViewById(R.id.favorite_button);
    }

    /**
     * Sets the title of the movie in the Activity top Action Bar
     *
     * @param title Selected movie title
     */
    public void setActionBarTitle(String title){

        setSupportActionBar( mToolbar);
        if (getSupportActionBar() !=null){
            getSupportActionBar().setTitle(title);
        }
    }
}
