package com.example.android.popularmovies.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.sync.MoviesAsyncTask;
import com.example.android.popularmovies.adapter.MoviesListAdapter;
import com.example.android.popularmovies.model.OnTaskCompleted;
import com.example.android.popularmovies.R;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Presents movie posters
     */
    private GridView mGridView;

    /**
     * Holds menu items
     */
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.v(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridView = findViewById(R.id.gridView);
        mGridView.setOnItemClickListener(moviePosterClickListener);

        if (savedInstanceState == null) {
            // Get data from the Internet
            getMoviesFromTMDb(getSortMethod());
        } else {
            // Get data from local resources
            // Get Movie objects
            Parcelable[] parcelable = savedInstanceState.
                    getParcelableArray(getString(R.string.parcel_movie));

            if (parcelable != null) {
                int numMovieObjects = parcelable.length;
                Movie[] movies = new Movie[numMovieObjects];
                for (int i = 0; i < numMovieObjects; i++) {
                    movies[i] = (Movie) parcelable[i];
                }

                // Load movie objects into view
                mGridView.setAdapter(new MoviesListAdapter(this, movies));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, mMenu);

        // Make menu items accessible
        mMenu = menu;

        // Add menu items
        mMenu.add(Menu.NONE, // No group
                R.string.pref_sort_pop_desc_key, // ID
                Menu.NONE, // Sort order: not relevant
                null) // No text to display
                .setVisible(false)
                .setIcon(R.drawable.ic_popular)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // Same settings as the one above
        mMenu.add(Menu.NONE, R.string.pref_sort_vote_avg_desc_key, Menu.NONE, null)
                .setVisible(false)
                .setIcon(R.drawable.icon_top_rated)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // Update menu to show relevant items
        updateMenu();

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Log.v(LOG_TAG, "onSaveInstanceState");

        int numMovieObjects = mGridView.getCount();
        if (numMovieObjects > 0) {
            // Get Movie objects from gridview
            Movie[] movies = new Movie[numMovieObjects];
            for (int i = 0; i < numMovieObjects; i++) {
                movies[i] = (Movie) mGridView.getItemAtPosition(i);
            }

            // Save Movie objects to bundle
            outState.putParcelableArray(getString(R.string.parcel_movie), movies);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.pref_sort_pop_desc_key:
                updateSharedPrefs(getString(R.string.tmdb_sort_pop_desc));
                updateMenu();
                getMoviesFromTMDb(getSortMethod());
                return true;
            case R.string.pref_sort_vote_avg_desc_key:
                updateSharedPrefs(getString(R.string.tmdb_sort_vote_avg_desc));
                updateMenu();
                getMoviesFromTMDb(getSortMethod());
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Listener for clicks on movie posters in GridView
     */
    private final GridView.OnItemClickListener moviePosterClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Movie movie = (Movie) parent.getItemAtPosition(position);

            Intent intent = new Intent(getApplicationContext(), MoviesDetailActivity.class);
            intent.putExtra(getResources().getString(R.string.parcel_movie), movie);

            startActivity(intent);
        }
    };

    private void getMoviesFromTMDb(String sortMethod) {
        if (isNetworkAvailable()) {
            // Key needed to get data from TMDb
            String apiKey = getString(R.string.key_themoviedb);
            OnTaskCompleted taskCompleted = new OnTaskCompleted() {
                @Override
                public void onMoviesRetrieveTaskCompleted(Movie[] movies) {
                    mGridView.setAdapter(new MoviesListAdapter(getApplicationContext(), movies));
                }
            };

            // Execute task
            MoviesAsyncTask movieTask = new MoviesAsyncTask(apiKey, taskCompleted);
            movieTask.execute(sortMethod);
        } else {
            Toast.makeText(this, getString(R.string.error_need_internet), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks if there is Internet accessible.
     * Based on a stackoverflow snippet
     *
     * @return True if there is Internet. False if not.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Update menu based on method found set in SharedPreferences
     */
    private void updateMenu() {
        String sortMethod = getSortMethod();

        if (sortMethod.equals(getString(R.string.tmdb_sort_pop_desc))) {
            mMenu.findItem(R.string.pref_sort_pop_desc_key).setVisible(false);
            mMenu.findItem(R.string.pref_sort_vote_avg_desc_key).setVisible(true);
        } else {
            mMenu.findItem(R.string.pref_sort_vote_avg_desc_key).setVisible(false);
            mMenu.findItem(R.string.pref_sort_pop_desc_key).setVisible(true);
        }
    }

    /**
     * Gets the sort method set by user from SharedPreferences. If no sort method is defined it will
     * default to sorting by popularity.
     *
     * @return Sort method from SharedPreferenced
     */
    private String getSortMethod() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        return prefs.getString(getString(R.string.pref_sort_method_key),
                getString(R.string.tmdb_sort_pop_desc));
    }

    /**
     * Saves the selected sort method
     *
     * @param sortMethod Sort method to save
     */
    private void updateSharedPrefs(String sortMethod) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_sort_method_key), sortMethod);
        editor.apply();
    }
}
