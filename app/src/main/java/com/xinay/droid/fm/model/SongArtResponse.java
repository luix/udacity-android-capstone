package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by luisvivero on 1/4/16.
 */
public class SongArtResponse {

    @SerializedName("result")
    List<SongArt> result;

    String key;

    int position;

    public SongArt getSongArt() {
        if (result != null) {
            return result.get(0);
        }
        return null;
    }

    public class SongArt implements Serializable {
        @SerializedName("arturl")
        String artUrl;

        @SerializedName("artist")
        String artist;

        @SerializedName("title")
        String title;

        @SerializedName("album")
        String album;

        @SerializedName("size")
        int size;



        public String getArtUrl() {
            return artUrl;
        }

        public String getArtist() {
            return artist;
        }

        public String getTitle() {
            return title;
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
