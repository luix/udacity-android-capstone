package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * Created by luisvivero on 7/12/15.
 */
@Parcel
public class Image implements Serializable {
    @SerializedName("url")
     String url;

    public String getUrl() {
        return url;
    }
}
