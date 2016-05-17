package com.example.android.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.adapter.CursorRecyclerViewAdapter;
import com.example.android.popularmovies.data.model.Review;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Adapter that exposes movie reviews data from a Cursor to a RecyclerView
 * which will hold the list of user reviews.
 */
public class ReviewCursorAdapter extends CursorRecyclerViewAdapter<ReviewCursorAdapter.ViewHolder> {

    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mReviewAuthor;
        public TextView mReviewDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            mReviewAuthor = (TextView) itemView.findViewById(R.id.list_review_author);
            mReviewDescription = (TextView) itemView.findViewById(R.id.list_review_description);
            mReviewAuthor.setClickable(true);
            mReviewDescription.setClickable(true);
            mReviewAuthor.setOnClickListener(this);
            mReviewDescription.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Show a dialog with the complete text of the User Review when the content or author
            // textview is clicked.
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Review from: " + mReviewAuthor.getText())
                    .setMessage(mReviewDescription.getText())
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

    public ReviewCursorAdapter(Context context, Cursor cursor){
        super(cursor);
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View trailerView = inflater.inflate(R.layout.review_itemview, parent, false);
        return new ViewHolder(trailerView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {

        Review review = null;
        try {
            review = Review.inflateFromCursor(cursor);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(review != null) {
            holder.mReviewAuthor.setText(review.getAuthor());
            holder.mReviewDescription.setText(review.getContent());
        }
    }
}
