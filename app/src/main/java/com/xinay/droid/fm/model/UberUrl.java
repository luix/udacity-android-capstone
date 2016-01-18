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
    private String url;

    @SerializedName("encoding")
    private String encoding;

    @SerializedName("callsign")
    private String callSign;

    @SerializedName("websiteurl")
    private String websiteUrl;

    @SerializedName("station_id")
    private int stationId;

    public String getUrl() {
        return url;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getCallSign() {
        return callSign;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public int getStationId() {
        return stationId;
    }
}
