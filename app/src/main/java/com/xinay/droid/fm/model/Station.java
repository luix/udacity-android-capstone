package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * Created by luisvivero on 1/3/16.
 */
@Parcel
public class Station implements Serializable {

    @SerializedName("callsign")
    String callSign;

    @SerializedName("genre")
    String genre;

    @SerializedName("artist")
    String artist;

    @SerializedName("title")
    String title;

    @SerializedName("songstamp")
    String songStamp;

    @SerializedName("seconds_remaining")
    int secondsRemaining;

    @SerializedName("station_id")
    String stationId;

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
