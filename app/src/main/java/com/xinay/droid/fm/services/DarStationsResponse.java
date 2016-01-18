package com.xinay.droid.fm.services;

import com.google.gson.annotations.SerializedName;
import com.xinay.droid.fm.model.Song;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by luisvivero on 1/4/16.
 */
public class DarStationsResponse {

    @SerializedName("stations")
    List<Station> stations;

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    @Parcel
    public static class Station {
        @SerializedName("station_id")
        int stationId;

        @SerializedName("callsign")
        String callSign;

        @SerializedName("dial")
        String dial;

        @SerializedName("band")
        String band;

        @SerializedName("address1")
        String address1;

        @SerializedName("address2")
        String address2;

        @SerializedName("city")
        String city;

        @SerializedName("description")
        String description;
    }
}
