package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by luisvivero on 1/4/16
 */
@Parcel
public class TopSongsResponse {
    @SerializedName("result")
     List<Song> songs;

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
