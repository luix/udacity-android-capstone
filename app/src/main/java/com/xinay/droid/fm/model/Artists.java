package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luisvivero on 7/12/15.
 */
@Parcel
public class Artists {

    @SerializedName("items")
     List<Artist> items;

    @SerializedName("total")
     int total;

    public Artists() {
        this.items = new ArrayList<Artist>();
    }

    public List<Artist> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }
}
