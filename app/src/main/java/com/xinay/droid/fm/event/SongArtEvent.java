package com.xinay.droid.fm.event;

import com.xinay.droid.fm.model.SongArtResponse;
import com.xinay.droid.fm.model.TopSongsResponse;

/**
 * Created by luisvivero on 9/7/15.
 */
public class SongArtEvent {

    public SongArtResponse response;

    public SongArtEvent(SongArtResponse songArtResponse) {
        this.response = songArtResponse;
    }
}
