package com.example.flixster.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Movie {

    //values from API
    String title;
    String overview;
    String posterPath; //only the path
    String backdropPath;
    Double voteAverage;
    Integer id;
    String popularity;

    //initialize from JSON data
    public Movie(JSONObject object) throws JSONException {
        title =  object.getString("title");
        overview = object.getString("overview");
        posterPath = object.getString("poster_path");
        backdropPath = object.getString("backdrop_path");
        voteAverage = object.getDouble("vote_average");
        id = object.getInt("id");
        popularity = object.getString("popularity");

    }

    public Movie(){}

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public Integer getId() { return id; }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public String getPopularity() { return popularity; }
}
