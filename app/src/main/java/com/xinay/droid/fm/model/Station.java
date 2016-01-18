package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by luisvivero on 1/3/16.
 */
public class Station implements Serializable {

    @SerializedName("callsign")
    private String callSign;

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

    @SerializedName("station_id")
    private String stationId;

    public String getCallSign() {
        return callSign;
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

    public String getStationId() {
        return stationId;
    }
}
