package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by luisvivero on 1/4/16.
 */
public class SongPlaylist implements Serializable {

    @SerializedName("callsign")
    private String callSign;

    @SerializedName("station_id")
    private int stationId;

    @SerializedName("genre")
    private String genre;

    @SerializedName("artist")
    private String artist;

    @SerializedName("title")
    private String title;

    @SerializedName("songstamp")
    private String songStamp;

    @SerializedName("seconds_remaining")
    private int secondsRemaining;


    public String getCallSign() {
        return callSign;
    }

    public int getStationId() {
        return stationId;
    }

    public String getGenre() {
        return genre;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getSongStamp() {
        return songStamp;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }
}
