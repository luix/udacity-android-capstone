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
    // song
    private Song song;
    // player binder
    private final IBinder playerBinder = new PlayerBinder();

    @Override
    public void onCreate() {
        // create the service
        super.onCreate();
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

    public void setSong(Song song){
        Log.v(LOG_TAG, "playSong()");
        Log.v(LOG_TAG, "uber station url: " + song.getUberUrl());
        this.song = song;
        try{
            Log.v(LOG_TAG, "player.reset()");
            player.reset();
            String url = song.getUberUrl().getUrl();
            Log.v(LOG_TAG, "player.setDataSource() , url: " + url);
            player.setDataSource(url);
        }
        catch(Exception ex){
            Log.e(LOG_TAG, "Error setting data source", ex);
        }
        Log.v(LOG_TAG, "player.prepareAsync()");
        player.prepareAsync();
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
        Log.v(LOG_TAG, "onCompletion()");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v(LOG_TAG, "onError()");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //start playback
        mediaPlayer.start();
    }

    public void playPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
