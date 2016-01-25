package com.xinay.droid.fm.event;

import com.xinay.droid.fm.model.ArtistSearchResponse;
import com.xinay.droid.fm.model.TopSongsResponse;
import com.xinay.droid.fm.util.StringUtilities;

/**
 * Created by luisvivero on 9/7/15.
 */
public class TopSongsEvent {

    public TopSongsResponse response;

    public TopSongsEvent(TopSongsResponse topSongsResponse) {
        this.response = topSongsResponse;
    }
}
