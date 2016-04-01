package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by luisvivero on 7/4/15.
 */
@Parcel
public class Artist implements Serializable {
    @SerializedName("id")
     String id;

    @SerializedName("name")
     String name;

    @SerializedName("images")
     List<Image> images;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Image> getImages() {
        return images;
    }
}
