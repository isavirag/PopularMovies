package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

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

        ActionBar sb = getSupportActionBar();

        if(sb != null){
            sb.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //To return to the right position in the drop down list when the up button is clicked, close
        // the current movie detail activity (finish) and it will act the same as the back button.
        //(Default Android action is to create a new instance of the activity which doesn't set the
        //previously chosen list.
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
