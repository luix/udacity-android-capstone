package com.xinay.droid.fm.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.xinay.droid.fm.PlayerManager;
import com.xinay.droid.fm.R;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.receiver.NotificationBroadcast;
import com.xinay.droid.fm.util.Constants;
import com.xinay.droid.fm.util.Controls;

import org.parceler.Parcel;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

/**
 * Created by luisvivero on 9/8/15.
 */
public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    private final String LOG_TAG = PlayerService.class.getSimpleName();

    public static final String NOTIFY_DELETE = "com.xinay.droid.fm.delete";
    public static final String NOTIFY_PAUSE = "com.xinay.droid.fm.pause";
    public static final String NOTIFY_PLAY = "com.xinay.droid.fm.play";

    int NOTIFICATION_ID = 7013;

    // media player
    private MediaPlayer player;
    // song
    private Song song;
    // player binder
    private final IBinder playerBinder = new PlayerBinder();

    private ComponentName remoteComponentName;
    private RemoteControlClient remoteControlClient;
    AudioManager audioManager;
    Bitmap mDummyAlbumArt;

    @Override
    public void onCreate() {
        // create the service
        super.onCreate();
        // create player
        player = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

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

        try {
            Log.v(LOG_TAG, "setSong() , RegisterRemoteClient()");
            RegisterRemoteClient();
            Log.v(LOG_TAG, "setSong() , newNotification()");
            newNotification();

            Constants.PLAY_PAUSE_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String)msg.obj;
                    if(player == null)
                        return false;
                    if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
                        Constants.SONG_PAUSED = false;
                        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                        player.start();
                    }else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
                        Constants.SONG_PAUSED = true;
                        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                        player.pause();
                    }
                    newNotification();
                    try{
//                        MainActivity.changeButton();
//                        AudioPlayerActivity.changeButton();
                    }catch(Exception e){}
                    Log.d("TAG", "TAG Pressed: " + message);
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.v(LOG_TAG, "setSong() , UpdateMetadata()");
        UpdateMetadata();
        Log.v(LOG_TAG, "setSong() ,  remoteControlClient.setPlaybackState()");
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);

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
    public void onCompletion(MediaPlayer player) {
        Log.v(LOG_TAG, "onCompletion()");
    }

    @Override
    public boolean onError(MediaPlayer player, int what, int extra) {
        Log.v(LOG_TAG, "onError()");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //start playback
        mediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "onStartCommand()");
        try {
            RegisterRemoteClient();
            newNotification();

            Constants.PLAY_PAUSE_HANDLER = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String message = (String)msg.obj;
                    if(player == null)
                        return false;
                    if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
                        Constants.SONG_PAUSED = false;
                        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                        player.start();
                    }else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
                        Constants.SONG_PAUSED = true;
                        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                        player.pause();
                    }
                    newNotification();
                    try{
//                        MainActivity.changeButton();
//                        AudioPlayerActivity.changeButton();
                    }catch(Exception e){}
                    Log.d("TAG", "TAG Pressed: " + message);
                    return false;
                }
            });

        } catch (Exception e) {
            Log.e(LOG_TAG, "onStartCommand exception : " + e);
            e.printStackTrace();
        }
        return START_STICKY;
    }

    private Bitmap albumArt;

    private void newNotification() {
        Log.v(LOG_TAG, "newNotification()");
        if (song == null) {
            song = PlayerManager.getInstance().getCurrentSong();
        }
        String songName = song.getSongTitle();
        String albumName = song.getSongArtist();
        RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.notification_layout);
        RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification_layout);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(songName).build();

        setListeners(simpleContentView);
        setListeners(expandedView);

        notification.contentView = simpleContentView;
        notification.bigContentView = expandedView;

        try{
            String albumArtUrl = song.getAlbumArtUrl();

            albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.droid_fm);

            if (albumArtUrl.indexOf("dar.fm") == -1) {
                Picasso.with(getApplicationContext())
                        .load(albumArtUrl)
                        .resize(100, 100)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                                albumArt = bitmap;
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                // Nothing else to do in this case
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                // Nothing else to do in this case
                            }
                        });
            }

            if(albumArt != null){
                notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
                notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
            }else{
                notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.droid_fm);
                notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.droid_fm);
            }
        }catch(Exception e){
            Log.e(LOG_TAG, "newNotification exception : " + e);
            e.printStackTrace();
        }
        if(Constants.SONG_PAUSED){
            notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

            notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
            notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
        }else{
            notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

            notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
        }

        notification.contentView.setTextViewText(R.id.textSongName, songName);
        notification.contentView.setTextViewText(R.id.textAlbumName, albumName);

        notification.bigContentView.setTextViewText(R.id.textSongName, songName);
        notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
    }

    public void setListeners(RemoteViews view) {
        Log.v(LOG_TAG, "setListeners()");
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent play = new Intent(NOTIFY_PLAY);

        PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy()");
        if(player != null){
            player.stop();
            player = null;
        }
        super.onDestroy();
    }

//    private void playSong(String songPath) {
//        try {
//            UpdateMetadata();
//            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
//            player.reset();
//            player.setDataSource(songPath);
//            player.prepare();
//            player.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void RegisterRemoteClient(){
        Log.v(LOG_TAG, "RegisterRemoteClient()");
        remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
        try {
            if(remoteControlClient == null) {
                audioManager.registerMediaButtonEventReceiver(remoteComponentName);
                Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                mediaButtonIntent.setComponent(remoteComponentName);
                PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
                remoteControlClient = new RemoteControlClient(mediaPendingIntent);
                audioManager.registerRemoteControlClient(remoteControlClient);
            }
            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP |
                            RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        }catch(Exception ex) {
            Log.e(LOG_TAG, "RegisterRemoteClient exception : " + ex);
        }
    }


    private void UpdateMetadata(){
        Log.v(LOG_TAG, "UpdateMetadata()");
        if (remoteControlClient == null)
            return;
        RemoteControlClient.MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, song.getAlbumArtUrl());
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, song.getSongArtist());
        metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.getSongTitle());
        if(mDummyAlbumArt == null){
            mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.droid_fm);
        }
        metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
        metadataEditor.apply();
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {}
}
