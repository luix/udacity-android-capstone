package com.xinay.droid.fm.event;

import com.xinay.droid.fm.model.ArtistSearchResponse;

/**
 * Created by luisvivero on 9/7/15.
 */
public class ArtistSearchEvent {

    public ArtistSearchResponse response;

    public ArtistSearchEvent(ArtistSearchResponse searchResponse) {
        this.response = searchResponse;
    }
}
