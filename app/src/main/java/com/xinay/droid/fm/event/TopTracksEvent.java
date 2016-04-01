package com.xinay.droid.fm.event;

import com.xinay.droid.fm.model.TopTracksResponse;

/**
 * Created by luisvivero on 9/7/15.
 */
public class TopTracksEvent {

    public TopTracksResponse response;

    public TopTracksEvent(TopTracksResponse searchResponse) {
        this.response = searchResponse;
    }
}
