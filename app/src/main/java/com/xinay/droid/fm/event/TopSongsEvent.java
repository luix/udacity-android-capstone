package com.xinay.droid.fm.event;

import com.xinay.droid.fm.model.TopSongsResponse;

/**
 * Created by luisvivero on 9/7/15.
 */
public class TopSongsEvent {

    public TopSongsResponse response;

    public TopSongsEvent(TopSongsResponse topSongsResponse) {
        this.response = topSongsResponse;
    }
}
