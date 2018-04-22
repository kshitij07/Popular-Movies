package com.example.android.popularmovies.sync;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.popularmovies.model.OnTaskCompleted;
import com.example.android.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MoviesAsyncTask extends AsyncTask<String, Void, Movie[]> {

    private final String mApiKey;

    private final OnTaskCompleted onTaskCompleted;

    private final String LOG_TAG = MoviesAsyncTask.class.getSimpleName();

    public MoviesAsyncTask(String mApiKey, OnTaskCompleted onTaskCompleted) {
        this.mApiKey = mApiKey;
        this.onTaskCompleted = onTaskCompleted;
    }

    @Override
    protected Movie[] doInBackground(String... strings) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        try {
            URL url = getApiUrl(strings);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();

            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            if (builder.length() == 0)
            {
                return null;
            }
            moviesJsonStr = builder.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
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
            return getMoviesDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private URL getApiUrl(String[] parameters) throws MalformedURLException {
        final String TMDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
        final String SORT_BY_PARAM = "sort_by";
        final String API_KEY_PARAM = "api_key";

        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendQueryParameter(SORT_BY_PARAM, parameters[0])
                .appendQueryParameter(API_KEY_PARAM, mApiKey)
                .build();

        return new URL(builtUri.toString());
    }

    private Movie[] getMoviesDataFromJson(String moviesJsonStr) throws JSONException {
        final String TAG_RESULTS = "results";
        final String TAG_ORIGINAL_TITLE = "original_title";
        final String TAG_POSTER_PATH = "poster_path";
        final String TAG_OVERVIEW = "overview";
        final String TAG_VOTE_AVERAGE = "vote_average";
        final String TAG_RELEASE_DATE = "release_date";
        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray resultsArray = moviesJson.getJSONArray(TAG_RESULTS);

        Movie[] movies = new Movie[resultsArray.length()];

        for (int i = 0; i < resultsArray.length(); i++) {
            movies[i] = new Movie();

            JSONObject movieInfo = resultsArray.getJSONObject(i);

            movies[i].setOriginalTitle(movieInfo.getString(TAG_ORIGINAL_TITLE));
            movies[i].setPosterPath(movieInfo.getString(TAG_POSTER_PATH));
            movies[i].setOverview(movieInfo.getString(TAG_OVERVIEW));
            movies[i].setVoteAverage(movieInfo.getDouble(TAG_VOTE_AVERAGE));
            movies[i].setReleaseDate(movieInfo.getString(TAG_RELEASE_DATE));
            Log.i("movies", String.valueOf(movieInfo));
        }

        return movies;
    }

    @Override
    protected void onPostExecute(Movie[] movies) {
        super.onPostExecute(movies);

        // Notify UI
        onTaskCompleted.onMoviesRetrieveTaskCompleted(movies);
    }
}
