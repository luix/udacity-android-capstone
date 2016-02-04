package com.xinay.droid.fm;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.xinay.droid.fm.R;
import com.xinay.droid.fm.async.RadioStationsClient;
import com.xinay.droid.fm.model.Artist;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.services.PlayerService;
import com.xinay.droid.fm.util.Constants;

import org.parceler.Parcel;
import org.parceler.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luisvivero on 1/17/16.
 */
@Parcel
public class PlayerManager {

    @Transient private final String LOG_TAG = PlayerManager.class.getSimpleName();

    private static final String ARG_TRACKS = "tracks";
    private static final String ARG_SONGS = "songs";
    private static final String ARG_SERVICE = "service";
    private static final String ARG_ARTIST_NAME = "artist_name";
    private static final String ARG_SEARCH_QUERY = "search_query";
    private static final String DEFAULT_SEARCH_QUERY = "Music";

    public static final String[] GENRES_MAP_KEYS = {
            "Music",
            "Rock",
            "80",
            "90",
            "Hip Hop",
            "Classical"
    };

    // The Singleton instance of the PlayerManager Object
    private static PlayerManager instance;

    // Used for all API calls to Radio Stations Web Services
    @Transient private RadioStationsClient radioStationsClient;
    // Used for the MediaPlayer interaction
    @Transient private PlayerService playerService;

    // Genres Map Songs
    Map<String, List<Song>> genresMapSongs;
    // Play Now Songs
    List<Song> songs;

    // current song playing, paused or selected for start
    Song currentSong;

    String searchQuery = "Music";

    int trackIndex;

    boolean playerBound = false;

    // Private constructor prevents any other class from instantiating
    private PlayerManager () {
        genresMapSongs = new HashMap<>();
        for (int i = 0; i < GENRES_MAP_KEYS.length; i++) {
            genresMapSongs.put(GENRES_MAP_KEYS[i], new ArrayList<Song>());
        }
    }

    // Providing a Global point of access for our PlayerManager Singleton
    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public RadioStationsClient getRadioStationsClient() {
        return radioStationsClient;
    }

    // Music Service connection
    @Transient private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(LOG_TAG, "ServiceConnection - onServiceConnected()...");
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            //get service
            playerService = binder.getService();
            //pass list
            //playerService.setList(tracks);
            playerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(LOG_TAG, "ServiceConnection - onServiceDisconnected()...");
            playerBound = false;
        }
    };

    public ServiceConnection getMusicConnection() {
        return musicConnection;
    }

    public PlayerService getPlayerService() {
        return playerService;
    }

    // PlayerManager lifecycle

    public void init() {
        radioStationsClient = new RadioStationsClient();
        searchQuery = "Music";
    }

    public void startup() {

        for (int i = 0; i < GENRES_MAP_KEYS.length; i++) {
            radioStationsClient.doTopSongs(GENRES_MAP_KEYS[i]);
        }

//        radioStationsClient.doTopSongs("Music");
//        radioStationsClient.doTopSongs("Rock");
//        radioStationsClient.doTopSongs("Hip Hop");
//        radioStationsClient.doTopSongs("80");
//        radioStationsClient.doTopSongs("90");
//        radioStationsClient.doTopSongs("00");
//        radioStationsClient.doTopSongs("Classical");
    }

    public void onPlayerPlayPause() {
        Log.v(LOG_TAG, "onPlayerPlayPause");
        if (playerService != null) {
            playerService.playPause();
        }
    }

    public void onPrepareSong(Song song) {
        playerService.setSong(song);
    }

    public boolean isPlaying() {
        if (playerService != null) {
            return playerService.isPlaying();
        } else {
            return false;
        }
    }

//    public Song onPlayerNext() {
//        Log.v(LOG_TAG, "onPlayerNext");
//        if (playerService != null) {
//            playerService.next();
//        }
//        trackIndex++;
//        if (trackIndex >= songs.size()) trackIndex = 0;
//        return songs.get(trackIndex);
//    }

//    public Song onPlayerPrev() {
//        Log.v(LOG_TAG, "onPlayerPrev");
//        if (playerService != null) {
//            playerService.prev();
//        }
//        trackIndex--;
//        if (trackIndex < 0) trackIndex = songs.size() - 1;
//        return songs.get(trackIndex);
//    }


    // Getters & Setters

    public void setSongs(List<Song> songs) {
        Log.v(LOG_TAG, "setTracks www - songs.size()=" + songs.size());
        this.songs = songs;
//        if (playerService != null) {
//            Log.v(LOG_TAG, "setSongs - songs.size()=" + songs.size());
//            playerService.setList(songs);
//        }

        if (currentSong == null) currentSong = songs.get(0);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public Song getCurrentSong() {
        Log.v(LOG_TAG, "currentSong()");
        if (currentSong == null) {
            if (songs != null) {
                Log.v(LOG_TAG, "setSongs - songs.size()=" + songs.size());
                currentSong = songs.get(0);
                Log.v(LOG_TAG, "currentSong - title: " + currentSong.getSongTitle());
            }
        }
        return currentSong;
    }

//    public Map<String, List<Song>> getGenresMapSongs() {
//        return genresMapSongs;
//    }
//
//    public void setGenresMapSongs(Map<String, List<Song>> genresMapSongs) {
//        this.genresMapSongs = genresMapSongs;
//    }

    public void setSongsByGenre(String key, List<Song> songs) {
        for (Song song : songs) {
            song.setGroupKey(key);
        }
        genresMapSongs.get(key).addAll(songs);

//        if (playerService != null) {
//            Log.v(LOG_TAG, "setSongs - songs.size()=" + songs.size());
//            playerService.setList(songs);
//        }

        if (currentSong == null) currentSong = songs.get(0);
    }

    public List<Song> getSongsByGenre(String key) {
        if (genresMapSongs.containsKey(key)) {
            return genresMapSongs.get(key);
        } else {
            return null;
        }
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public int getTrackIndex() {
        return trackIndex;
    }
}
