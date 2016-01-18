package com.xinay.droid.fm.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * Created by luisvivero on 1/4/16.
 */
@Parcel
public class UberUrl implements Serializable {

    @SerializedName("url")
    String url;

    @SerializedName("encoding")
    String encoding;

    @SerializedName("callsign")
    String callSign;

    @SerializedName("websiteurl")
    String websiteUrl;

    @SerializedName("station_id")
    int stationId;

    public String getUrl() {
        return url;
    }

    public String getEncoding() {
        return encoding;
    }
}
