package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * Created by luisvivero on 1/3/16.
 */
@Parcel
public class Song implements Serializable {

    @SerializedName("songartist")
    String songArtist;

    @SerializedName("songtitle")
    String songTitle;

    @SerializedName("currently_playing")
    String currentlyPlaying;

    @SerializedName("callsign")
    String callSign;

    @SerializedName("station_id")
    String stationId;

    @SerializedName("playlist")
    Playlist playlist;

    @SerializedName("uberurl")
    UberUrl uberUrl;

    String albumArtUrl;

    String groupKey;

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public void setAlbumArtUrl(String albumArtUrl) {
        this.albumArtUrl = albumArtUrl;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getCurrentlyPlaying() {
        return currentlyPlaying;
    }

    public String getCallSign() {
        return callSign;
    }

    public String getStationId() {
        return stationId;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public UberUrl getUberUrl() {
        return uberUrl;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
}
