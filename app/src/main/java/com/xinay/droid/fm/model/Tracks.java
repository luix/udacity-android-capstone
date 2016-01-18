package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luisvivero on 7/13/15.
 */
@Parcel
public class Tracks {

    @SerializedName("items")
     List<Track> items;

    @SerializedName("total")
     int total;

    public Tracks() {
        this.items = new ArrayList<Track>();
    }

    public List<Track> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }
}
