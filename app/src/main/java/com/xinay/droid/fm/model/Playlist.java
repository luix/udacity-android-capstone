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

    @SerializedName("items")
    List<Station> items;

    @SerializedName("total")
    int total;

    public Playlist() {
        this.items = new ArrayList<Station>();
    }

    public List<Station> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }
}
