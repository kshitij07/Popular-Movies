package com.example.android.popularmovies.model;

import com.example.android.popularmovies.model.Movie;

/**
 * Created by Kshitij on 14-04-2018.
 */

public interface OnTaskCompleted {
    void onMoviesRetrieveTaskCompleted(Movie[] movies);
}
