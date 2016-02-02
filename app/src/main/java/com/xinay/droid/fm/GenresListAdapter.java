package com.xinay.droid.fm;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.model.Playlist;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Station;
import com.xinay.droid.fm.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by luisvivero on 24/1/16.
 */
public class GenresListAdapter extends RecyclerView.Adapter<GenresListAdapter.ViewHolder> {

    final static String LOG_TAG = GenresListAdapter.class.getSimpleName();

    private Context context;
    // track songs
    private Map<String, Song> songs;
    private List<String> keys;

    static MainActivity parentActivity;

//    public GenresListAdapter() {
//        Log.v(LOG_TAG, "GenresListAdapter - empty constructor");
//    }

    public GenresListAdapter(MainActivity activity) {
        Log.v(LOG_TAG, "GenresListAdapter - non-empty constructor");
        parentActivity = activity;
    }

    public void setSongs(Map<String, Song> songs) {
        Log.v(LOG_TAG, "setTracks www - songs.size()=" + songs.size());
        this.songs = songs;
        keys = new ArrayList<>();
        keys.addAll(songs.keySet());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Log.v(LOG_TAG, "onCreateViewHolder - " + this.toString());
        context = viewGroup.getContext();
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.card_song, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder");
        if (songs == null || viewHolder == null) {
            return;
        }

        String key = keys.get(position);
        Log.v(LOG_TAG, "song key " + key);

        Song song = songs.get(key);
        if (song != null) {
            viewHolder.setSong(song);
            viewHolder.itemSongTitle.setText(song.getSongTitle());
            viewHolder.itemArtistName.setText(song.getSongArtist());
            viewHolder.itemStationCallSign.setText(song.getCallSign());
            Log.v(LOG_TAG, "song title " + song.getSongTitle());
            Log.v(LOG_TAG, "song artist : " + song.getSongArtist());
            Log.v(LOG_TAG, "albumArtUrl: " + song.getAlbumArtUrl());
            Log.v(LOG_TAG, "song group key " + song.getGroupKey());
            if (song.getAlbumArtUrl() != null) {
                // substitute default image from dar.fm with droid.fm logo
                if (song.getAlbumArtUrl().indexOf("dar.fm") != -1) {
                    viewHolder.itemAlbumArt.setImageResource(R.drawable.droid_fm);
                } else {
                    Picasso.with(context).setIndicatorsEnabled(true);
                    Picasso.with(context)
                            .load(song.getAlbumArtUrl())
                            .placeholder(R.drawable.droid_fm)
                            .error(R.drawable.droid_fm)
                            .fit()
                            .into(viewHolder.itemAlbumArt);
                }
            } else {
                PlayerManager.getInstance().getRadioStationsClient().doSongArt(
                        song.getSongArtist(),
                        song.getSongTitle(),
                        Constants.ALBUM_ART_IMAGE_RESOLUTION,
                        key
                );
            }
        }
    }

    @Override
    public int getItemCount() {
        if (songs != null) {
            return songs.size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private final ImageView itemAlbumArt;
        private final TextView itemSongTitle;
        private final TextView itemArtistName;
        private final TextView itemStationCallSign;
        private Song song;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            itemAlbumArt = (ImageView) view.findViewById(R.id.album_art);
            itemSongTitle = (TextView) view.findViewById(R.id.song_title);
            itemArtistName = (TextView) view.findViewById(R.id.artist_name);
            itemStationCallSign = (TextView) view.findViewById(R.id.station_call_sign);
        }

        @Override
        public void onClick(View v) {
            Log.v(LOG_TAG, "onClick - song title: " + song.getSongTitle());
            //parentActivity.instantiateTopTracksFragment(itemArtist);
            parentActivity.onSongSelected(song);
        }

        public void setSong(Song song) {
            this.song = song;
        }
    }
}
