package com.xinay.droid.fm;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.model.Playlist;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Station;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.xinay.droid.fm.util.Constants.ALBUM_ART_IMAGE_RESOLUTION;
import static com.xinay.droid.fm.util.Constants.EXTRA_GENRE_KEY;
import static com.xinay.droid.fm.util.Constants.EXTRA_STARTING_ALBUM_POSITION;

/**
 * Created by luisvivero on 24/1/16.
 */
public class GenresListAdapter extends RecyclerView.Adapter<GenresListAdapter.ViewHolder> {

    final static String LOG_TAG = GenresListAdapter.class.getSimpleName();

    private Context context;
    private String mGenre;

    static MainActivity parentActivity;

    public GenresListAdapter(MainActivity activity) {
        Log.v(LOG_TAG, "GenresListAdapter - non-empty constructor");
        parentActivity = activity;
    }

    public void setGenre(String genre) {
        this.mGenre = genre;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.card_song, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Song song = parentActivity.getPlayerManager().getSongsByGenre(mGenre).get(position);
        if (song != null) {
            viewHolder.setSong(song);
            viewHolder.itemSongTitle.setText(song.getSongTitle());
            viewHolder.itemArtistName.setText(song.getSongArtist());
            viewHolder.itemStationCallSign.setText(song.getCallSign());
            //Log.v(LOG_TAG, "song title " + song.getSongTitle());
            //Log.v(LOG_TAG, "song artist : " + song.getSongArtist());
            //Log.v(LOG_TAG, "albumArtUrl: " + song.getAlbumArtUrl());
            //Log.v(LOG_TAG, "song group key " + song.getGroupKey());
            if (song.getAlbumArtUrl() != null) {
                viewHolder.itemAlbumArt.setTransitionName(song.getUberUrl().getUrl());
                viewHolder.itemAlbumArt.setTag(song.getUberUrl().getUrl());
                viewHolder.mAlbumPosition = position;
                // substitute default image from dar.fm with droid.fm logo
                if (song.getAlbumArtUrl().indexOf("dar.fm") != -1) {
                    viewHolder.itemAlbumArt.setImageResource(R.drawable.droid_fm_thumbnail);
                } else {
                    Picasso.with(context).setIndicatorsEnabled(true);
                    Picasso.with(context)
                            .load(song.getAlbumArtUrl())
                            .placeholder(R.drawable.droid_fm_thumbnail)
                            .error(R.drawable.droid_fm_thumbnail)
                            .fit()
                            .into(viewHolder.itemAlbumArt);
                }
            } else {
                PlayerManager.getInstance().getRadioStationsClient().doSongArt(
                        song.getSongArtist(),
                        song.getSongTitle(),
                        ALBUM_ART_IMAGE_RESOLUTION,
                        position
                );
            }
        }
    }

    @Override
    public int getItemCount() {
        return parentActivity.getPlayerManager().getSongsByGenre(mGenre).size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private final ImageView itemAlbumArt;
        private final TextView itemSongTitle;
        private final TextView itemArtistName;
        private final TextView itemStationCallSign;
        private Song song;
        private int mAlbumPosition;
        private boolean cardClicked = false;

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
            //parentActivity.onSongSelected(song, itemAlbumArt);
            Log.v(LOG_TAG, "onClick - parentActivity.isDetailsActivityStarted(): " + String.valueOf(parentActivity.isDetailsActivityStarted()));

            // to prevent user from double clicking and start activity twice
            if (!parentActivity.isDetailsActivityStarted()) {

                parentActivity.getPlayerManager().setCurrentSong(song);
                parentActivity.getPlayerManager().setSongs(PlayerManager.getInstance().getSongsByGenre(song.getGroupKey()));

                Intent intent = new Intent(parentActivity, PlayerActivity.class);
                intent.putExtra(EXTRA_GENRE_KEY, song.getGroupKey());
                intent.putExtra(EXTRA_STARTING_ALBUM_POSITION, mAlbumPosition);

                Log.v(LOG_TAG, "parentActivity.startActivity() -  itemAlbumArt.getTransitionName: " + itemAlbumArt.getTransitionName());
                parentActivity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(parentActivity,
                        itemAlbumArt, itemAlbumArt.getTransitionName()).toBundle());
                //parentActivity.startActivity(intent);

                parentActivity.setDetailsActivityStarted(true);
            }
        }

        public void setSong(Song song) {
            this.song = song;
        }
    }
}
