package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

/**
 * MainActivity for Popular Movies App. In small devices, it contains solely the
 * main grid fragment (with movie posters) and in larger devices it shows a two pane layout with
 * the movie details on the right.
 */
public class MainActivity extends AppCompatActivity {

    public static final String MOVIEDETFRAGTAG = "MDF";

    private LinearLayout mEmptyLinearLayout;
    private LinearLayout mAllDetailsLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Switch the empty container with a new detail fragment
     * This is only used with two pane mode
     *
     * @param fragment the fragment that needs to be shown in the UI
     */
    public void switchContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment, MOVIEDETFRAGTAG).commit();
    }
}
