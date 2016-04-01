package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luisvivero on 1/3/16.
 */
@Parcel
public class Songs {
    @SerializedName("items")
    List<Song> items;

    @SerializedName("total")
    int total;

    public Songs() {
        this.items = new ArrayList<Song>();
    }

    public List<Song> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }
}
