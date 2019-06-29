package com.example.flixster.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Config {

    //the base url for loading images
    String imageBaseURL;
    //the poster size to use when fetching images, part of the URL
    String posterSize;
    //backdrop size
    String backdropSize;

    public Config(JSONObject object) throws JSONException {

        JSONObject images = object.getJSONObject("images");
        imageBaseURL = images.getString("secure_base_url");
        //get the poster size
        JSONArray posterSizeOptions = images.getJSONArray("poster_sizes");
        //use the option at index 3 or w342 as a fallback
        posterSize = posterSizeOptions.optString(3, "w342"); //declare a default for this before trying out what it's like
        //parse the backdrop size
        JSONArray backdropSizeOptions = images.getJSONArray("backdrop_sizes");
        backdropSize = backdropSizeOptions.optString(1, "w780");
    }

    public Config(){};

    public String getImageBaseURL() {
        return imageBaseURL;
    }

    public String getPosterSize() {
        return posterSize;
    }

    //helper method for creating urls
    public String getImageUrl(String size, String path) {
        return String.format("%s%s%s", imageBaseURL, size, path);
    }

    public String getBackdropSize() { return backdropSize; }
}
