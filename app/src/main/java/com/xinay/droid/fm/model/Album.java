package com.xinay.droid.fm.model;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by luisvivero on 7/13/15.
 */
@Parcel
public class Album implements Serializable {
     String id;

     String href;

     String name;

     String uri;

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

     List<Image> images;

    public String getId() {
        return id;
    }

    public String getHref() {
        return href;
    }

    public List<Image> getImages() {
        return images;
    }
}
