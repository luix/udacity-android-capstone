package com.xinay.droid.fm.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Track;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by luisvivero on 9/8/15.
 */
public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final String LOG_TAG = PlayerService.class.getSimpleName();

    // media player
    private MediaPlayer player;
    // track list
    private List<Track> tracks;
    // songs list
    private List<Song> songs;
    // current song position
    private int songIndex;
    // current track position
    private int trackIndex;
    // player binder
    private final IBinder playerBinder = new PlayerBinder();

    @Override
    public void onCreate() {
        // create the service
        super.onCreate();
        // initialize position
        trackIndex = 0;
        // create player
        player = new MediaPlayer();

        initPlayer();
    }

    public void initPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    /*public void setList(List<Track> tracks){
        this.tracks = tracks;
        for (int i = 0; i < tracks.size(); i++) {
            Track track = tracks.get(i);
            Log.v(LOG_TAG, "track[" + i + "] , name: " + track.getName() + " , preview: " + track.getPreviewUrl());
        }
    }*/

    public void setList(List<Song> songs){
        this.songs = songs;
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            Log.v(LOG_TAG, "song[" + i + "] , title: " + song.getSongTitle() + " , preview: " + song.getUberUrl());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //start playback
        mediaPlayer.start();
    }

    public void setTrack(int trackIndex){
        Log.v(LOG_TAG, "setTrack - trackIndex: " + trackIndex);
        this.trackIndex = trackIndex;
    }

    public void setSong(int songIndex){
        Log.v(LOG_TAG, "setSong - songIndex: " + songIndex);
        this.songIndex = songIndex;
    }

    public void playPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
    }

    public void next() {
        trackIndex++;
        if (trackIndex >= songs.size()) trackIndex = 0;
        playSong();
    }

    public void prev() {
        trackIndex--;
        if (trackIndex < 0) trackIndex = songs.size() - 1;
        playSong();
    }

    public void seekTo(int progress) {
        player.pause();
        player.seekTo(progress);
        player.start();
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public int getSongDuration() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void playSong(){
        try{
            player.reset();
            Song song = songs.get(trackIndex);
            String url = song.getUberUrl().getUrl();
            Log.v(LOG_TAG, "playSong() - track[" + trackIndex + "] , title: " + song.getSongTitle() + " , url: " + url);
            player.setDataSource(url);
        }
        catch(Exception ex){
            Log.e(LOG_TAG, "Error setting data source", ex);
        }
        player.prepareAsync();
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
