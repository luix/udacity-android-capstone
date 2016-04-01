package com.xinay.droid.fm.util;

import android.os.Handler;

/**
 * Created by luisvivero on 7/12/15.
 */
public class Constants {

//    public static final String API_URL = "https://api.spotify.com/v1";

    public static final String API_URL = "https://api.dar.fm";

    public static final String ALBUM_ART_IMAGE_RESOLUTION = "hi";

    public static final String TYPE_ARTISTS = "artist";

    public static final String ARTIST_ID_KEY = "artist_id_key";
    public static final String ARTIST_NAME_KEY = "artist_name_key";

    public static final String EXTRA_GENRE_KEY = "extra_genre_key";
    public static final String EXTRA_STARTING_ALBUM_POSITION = "extra_starting_item_position";
    public static final String EXTRA_CURRENT_ALBUM_POSITION = "extra_current_item_position";

    public static final String TRACK_ID_KEY = "track_id_key";
    public static final String TRACK_NAME_KEY = "track_name_key";

    public static final String SEARCH_RESULTS_INSTANCE_STATE = "search_results_instance_state";

    public static final String COUNTRY_CODE = "US";

    //song is playing or paused
    public static boolean SONG_PAUSED = true;
    //handler for song play/pause defined in service(PlayerService)
    public static Handler PLAY_PAUSE_HANDLER;

    public static final String[] GENRES_LIST = {
            "Music",
            "70's",
            "80's",
            "90's",
            "00's",
            "Rock",
            "Country",
            "World",
            "Latin Hits",
            "Hip Hop",
            "Chill",
            "Electronic",
            "House",
            "Techno",
            "Metal",
            "Classical"
    };



}
