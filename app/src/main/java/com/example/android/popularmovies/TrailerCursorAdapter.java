package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.adapter.CursorRecyclerViewAdapter;
import com.example.android.popularmovies.data.model.Trailer;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Adapter that exposes movie trailer data from a Cursor to a RecyclerView
 * which will hold the list of movie trailer images/links.
 */
public class TrailerCursorAdapter extends CursorRecyclerViewAdapter<TrailerCursorAdapter.ViewHolder> {

    //Constants
    final String YOUTUBE_IMG_BASE_URL = "http://img.youtube.com/vi/";
    final String IMG_EXTENSION = "0.jpg";

    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //Constants
        final String YOUTUBE_VID_BASE_URL = "http://www.youtube.com/watch?v=";

        public ImageView mTrailerImageView;
        public ImageView mPlayIconImageView;
        private String mYoutubeId;

        public ViewHolder(View itemView) {
            super(itemView);
            mTrailerImageView = (ImageView) itemView.findViewById(R.id.list_image_trailer);
            mPlayIconImageView = (ImageView) itemView.findViewById(R.id.list_image_play);
            mTrailerImageView.setClickable(true);
            mTrailerImageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            //If a Trailer Image or play button image is pressed, create an intent to open the video
            //link in the Youtube app or browser.
            Uri builtUri = Uri.parse(YOUTUBE_VID_BASE_URL).buildUpon()
                    .appendEncodedPath(mYoutubeId)
                    .build();
            v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, builtUri));
        }

        /**
         * This method is called by the TrailerCursorAdapter during OnBindViewHolder to set the
         * YoutubeId that will be used in the url when the image is clicked.
         *
         * @param youtubeId the ID of the video used in the youtube url
         */
        public void setYoutubeId(String youtubeId) {
            mYoutubeId = youtubeId;
        }

    }

    public TrailerCursorAdapter(Context context, Cursor cursor){
        super(cursor);
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View trailerView = inflater.inflate(R.layout.trailer_itemview, parent, false);
        return new ViewHolder(trailerView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {

        Trailer trailer = null;
        try {
            trailer = Trailer.inflateTrailerFromCursor(cursor);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(trailer != null) {
            String youtubeId = trailer.getYoutubeId();

            //Pass the youtubeId to the viewholder - it needs it to create the link when clicked
            holder.setYoutubeId(youtubeId);

            //Build the URI to retrieve the trailer thumbnail from youtube
            Uri builtUri = Uri.parse(YOUTUBE_IMG_BASE_URL).buildUpon()
                    .appendEncodedPath(youtubeId)
                    .appendEncodedPath(IMG_EXTENSION)
                    .build();

            //Load images using Picasso library
            Picasso.with(mContext).load(builtUri).into(holder.mTrailerImageView);
        }
    }
}
