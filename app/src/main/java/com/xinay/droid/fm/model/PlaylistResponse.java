package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by luisvivero on 1/3/16.
 */
public class PlaylistResponse {
    @SerializedName("result")
    Playlist playlist;

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

}
