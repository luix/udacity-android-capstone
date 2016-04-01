package com.xinay.droid.fm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xinay.droid.fm.R;
import com.xinay.droid.fm.model.Artist;
import com.xinay.droid.fm.model.Artists;
import com.xinay.droid.fm.model.Image;
import com.xinay.droid.fm.model.Playlist;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Station;
import com.xinay.droid.fm.model.TopSongsResponse;

/**
 * Created by luisvivero on 7/12/15.
 */
public class SearchResultsListAdapter extends RecyclerView.Adapter<SearchResultsListAdapter.ViewHolder> {

    private final String LOG_TAG = SearchResultsListAdapter.class.getSimpleName();

    private static MainActivity parentActivity;
    private Playlist playlist;

    public SearchResultsListAdapter() {
    }

    public SearchResultsListAdapter(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Log.v(LOG_TAG, "onCreateViewHolder");
        LayoutInflater inflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_search_result, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder");
        if (playlist == null) {
            return;
        }

        Station station = playlist.getStations().get(position);
        Log.v(LOG_TAG, "song title " + station.getTitle());

        viewHolder.setStation(station);
        Log.v(LOG_TAG, "song artist : " + station.getArtist());

        viewHolder.itemArtistName.setText(station.getArtist());

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
        private final TextView itemSongTitle;
        private final TextView itemArtistName;
        private final TextView itemStationCallSign;
        private final TextView itemGenre;
        private Station station;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            itemSongTitle = (TextView) view.findViewById(R.id.song_title);
            itemArtistName = (TextView) view.findViewById(R.id.artist_name);
            itemStationCallSign = (TextView) view.findViewById(R.id.station_call_sign);
            itemGenre = (TextView) view.findViewById(R.id.genre);
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
