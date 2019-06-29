package com.example.flixster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.flixster.models.Config;
import com.example.flixster.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    //constants
    //the base URL for API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    //the parameter name for the API key
    public final static String API_KEY_PARAM = "api_key";


    //tag for logging from this activity
    public final static String TAG = "MainActivity";

    //instance fields
    AsyncHttpClient client;
    //the list of currently playing movies
    ArrayList<Movie> movies;
    //the recycler view
    @BindView(R.id.rvMovies) RecyclerView rvMovies;
    //the adapter wired to the recycler view
    MovieAdapter adapter;
    //image config
    Config config;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //initialize the client
        client = new AsyncHttpClient();
        //initialize the list of movies
        movies = new ArrayList<>();
        //initialize the adapter -- movies array cannot be reinitialized after this point
        adapter = new MovieAdapter(movies);

        //resolve the recycler view and connect a layout manager and the adapter
        //rvMovies = (RecyclerView) findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);

        //get the configuration
        getConfiguration();
    }

    //get the list of currently playing movies from the API
    private void getNowPlaying(){
        //create the URL
        String url = API_BASE_URL + "/movie/now_playing";
        //set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); //API key, always required

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //load the results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    //iterate through result set and create Movie objects
                    for (int i = 0; i < results.length(); i++){
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        //notify adapter that a movie was added
                        adapter.notifyItemInserted(movies.size() - 1);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now_playing endpoint", throwable, true);
            }
        });
    }

    //get the configuration from API
    private void getConfiguration() {

        //create the URL
        String url = API_BASE_URL + "/configuration";
        //set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); //API key, always required
        //execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed getting configuration", throwable, true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //get the image base url
                try {
                    config = new Config(response);
                    String.format("Loaded configuration with imageBaseUrl %s and poster size %s", config.getImageBaseURL(), config.getPosterSize());

                    //pass config to adapter
                    adapter.setConfig(config);

                    //get the now playing movies, moved here because of asynchronous programming
                    getNowPlaying();

                } catch (JSONException e) {
                    logError("Failed parsing configuration", e, true);
                }
            }
        });
    }

    //handle errors and silent queueus
    private void logError(String message, Throwable error, boolean alertUser){
        //always log the error
        Log.e(TAG, message, error);
        //alert the user to avoid silent errors
        if(alertUser){
            //show a long toast with the error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
