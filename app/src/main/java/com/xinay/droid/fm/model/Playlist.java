package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luisvivero on 1/3/16.
 */
@Parcel
public class Playlist {

    List<Station> stations;

    public Playlist() {
        this.stations = new ArrayList<Station>();
    }

    public List<Station> getStations() {
        return stations;
    }
}
