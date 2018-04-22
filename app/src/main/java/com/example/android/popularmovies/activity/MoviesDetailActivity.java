package com.example.android.popularmovies.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.data.MovieDateTimeHelper;
import com.example.android.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;

/**
 * Created by Kshitij on 12-04-2018.
 */

public class MoviesDetailActivity extends AppCompatActivity {

    private final String LOG_TAG = MoviesDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        TextView txtMovieTitle = findViewById(R.id.txtMovieTitle);
        ImageView imageview_poster = findViewById(R.id.imageview_poster);
        TextView txtMovieDescription = findViewById(R.id.txtMovieDescription);
        TextView txtRating = findViewById(R.id.txtRating);
        TextView txtReleaseDate = findViewById(R.id.txtReleaseDate);

        Intent intent = getIntent();
        Movie movie = intent.getParcelableExtra(getString(R.string.parcel_movie));

        txtMovieTitle.setText(movie.getOriginalTitle());

        Picasso.with(this)
                .load(movie.getPosterPath())
                .resize(getResources().getInteger(R.integer.tmdb_poster_w185_width),
                        getResources().getInteger(R.integer.tmdb_poster_w185_height))
                .error(R.drawable.not_found)
                .placeholder(R.drawable.searching)
                .into(imageview_poster);

        String overView = movie.getOverview();
        if (overView == null) {
            txtMovieDescription.setTypeface(null, Typeface.ITALIC);
            overView = getResources().getString(R.string.no_summary_found);
        }
        txtMovieDescription.setText(overView);
        txtRating.setText(movie.getDetailedVoteAverage());

        String releaseDate = movie.getReleaseDate();
        if(releaseDate != null) {
            try {
                releaseDate = MovieDateTimeHelper.getLocalizedDate(this,
                        releaseDate, movie.getDateFormat());
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Error with parsing movie release date", e);
            }
        } else {
            txtReleaseDate.setTypeface(null, Typeface.ITALIC);
            releaseDate = getResources().getString(R.string.no_release_date_found);
        }
        txtReleaseDate.setText(releaseDate);
    }
}
