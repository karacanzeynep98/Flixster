package com.example.flixster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.flixster.models.Config;
import com.example.flixster.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MovieDetailsActivity extends AppCompatActivity {

    //constants
    //the base URL for API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    //the parameter name for the API key
    public final static String API_KEY_PARAM = "api_key";

    //tag for logging from this activity
    public final static String TAG = "MovieDetailsActivity";

    //the adapter wired to the details activity
    MovieAdapter adapter;

    //instance fields
    AsyncHttpClient client;
    //image config
    Config config;
    // the movie to display
    Movie movie;

    //key
    String my_key;
    //id
    Integer id;

    // the view objects
    TextView tvTitle;
    TextView tvOverview;
    TextView popularityText;
    RatingBar rbVoteAverage;
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        // resolve the view objects
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);
        imgView = (ImageView) findViewById(R.id.imageToVideo);
        popularityText = (TextView) findViewById(R.id.popularityText);
        client = new AsyncHttpClient();

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        config = (Config) Parcels.unwrap(getIntent().getParcelableExtra(Config.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        id = movie.getId();

        // set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());
        popularityText.setText(movie.getPopularity());

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
        getMovieVideos();

        imgView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(my_key!=null){
                    Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                    // pass key (youtube video id) to MovieTrailerActivity -- use Intent
                    intent.putExtra("key", my_key);
                    startActivity(intent);
                }

            }
        });

        //build url for poster image
        String imageUrl = null;

        imageUrl = config.getImageUrl(config.getPosterSize(), movie.getPosterPath());

        //get the correct placeholder and imageview from the location
        int placeholderId = R.drawable.flicks_movie_placeholder;
        ImageView imageView = imgView;


        //load image using glide
        Glide.with(this)
                .load(imageUrl)
                .bitmapTransform(new RoundedCornersTransformation(this, 25, 0))
                .placeholder(placeholderId)
                .error(placeholderId)
                .into(imageView);

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



    //get the list of currently playing movies from the API
    private void getMovieVideos(){

        //create the additional URL
        String addUrl = API_BASE_URL + "/movie/" + id + "/videos";

        //set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); //API key, always required

        client.get(addUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //load the results into movies list
                // make api call to movies/{$movie.id}/videos
                try {
                    JSONArray results = response.getJSONArray("results");

                    //create object assigner
                    // parse response and extract "key" from response
                    JSONObject object = results.getJSONObject(0);

                    my_key = object.getString("key");

                    Log.i(TAG, String.format("Loaded %s objects", results.length()));

                } catch (JSONException e) {
                    logError("Failed to parse trailer list", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now_playing endpoint", throwable, true);
            }
        });
    }



}
