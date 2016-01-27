package com.xinay.droid.fm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinay.droid.fm.model.Playlist;
import com.xinay.droid.fm.model.Station;

/**
 * Created by luisvivero on 24/1/16.
 */
public class GenresListAdapter extends RecyclerView.Adapter<GenresListAdapter.ViewHolder> {

    private final String LOG_TAG = GenresListAdapter.class.getSimpleName();

    private static MainActivity parentActivity;
    private Playlist playlist;

    public GenresListAdapter() {
    }

    public GenresListAdapter(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Log.v(LOG_TAG, "onCreateViewHolder");
        LayoutInflater inflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_song, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder");
        if (playlist == null || viewHolder == null) {
            return;
        }

        Station station = playlist.getStations().get(position);
        Log.v(LOG_TAG, "song title " + station.getTitle());

        viewHolder.setStation(station);
        Log.v(LOG_TAG, "song artist : " + station.getArtist());

        viewHolder.itemSongTitle.setText(station.getTitle());
        viewHolder.itemArtistName.setText(station.getArtist());
        viewHolder.itemStationCallSign.setText(station.getCallSign());

        /*
        Log.v(LOG_TAG, "are images null : "  + String.valueOf(artist.getImages() == null));

        if (artist.getImages() != null && artist.getImages().size() > 0) {

            Log.v(LOG_TAG, "get first image");

            Image image = artist.getImages().get(0);

            // check for a valid Album Art URL
            if (Patterns.WEB_URL.matcher(image.getUrl()).matches()) {
                Picasso.with(parentActivity.getApplicationContext())
                        .load(image.getUrl())
                        .into(viewHolder.itemArtistThumbnail);
            } else {
                // Set a default image if thumbnail not found or not valid
                viewHolder.itemArtistThumbnail.setImageResource(R.mipmap.ic_launcher);
            }
        }
        */
    }

    @Override
    public int getItemCount() {
        if (playlist != null) {
            return playlist.getStations().size();
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
        private Station station;

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
            //parentActivity.instantiateTopTracksFragment(itemArtist);
            parentActivity.onStationSelected(station);
        }

        public void setStation(Station station) {
            station = station;
        }
    }
}
