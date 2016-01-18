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
import com.xinay.droid.R;
import com.xinay.droid.fm.model.Artist;
import com.xinay.droid.fm.model.Artists;
import com.xinay.droid.fm.model.Image;
import com.xinay.droid.fm.model.Playlist;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.TopSongsResponse;

/**
 * Created by luisvivero on 7/12/15.
 */
public class SearchResultsListAdapter extends RecyclerView.Adapter<SearchResultsListAdapter.ViewHolder> {

    private final String LOG_TAG = SearchResultsListAdapter.class.getSimpleName();

    private static MainActivity parentActivity;
    private Artists artists;
    private TopSongsResponse items;

    public SearchResultsListAdapter() {
    }

    public SearchResultsListAdapter(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void setArtists(Artists artists) {
        this.artists = artists;
    }

    public void setItems(TopSongsResponse items) {
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Log.v(LOG_TAG, "onCreateViewHolder");
        LayoutInflater inflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.artist_search_result, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder");
        if (items == null) {
            return;
        }

        Song song = items.getSongs().get(position);
        Log.v(LOG_TAG, "song title " + song.getSongTitle());

        viewHolder.setSong(song);
        Log.v(LOG_TAG, "song artist : " + song.getSongArtist());
        Log.v(LOG_TAG, "song call sign : " + song.getCallSign());
        Log.v(LOG_TAG, "song current playing : " + song.getCurrentlyPlaying());
        Log.v(LOG_TAG, "song station id : " + song.getStationId());
        Log.v(LOG_TAG, "song uber url : " + song.getUberUrl());
        Playlist playlist = song.getPlaylist();
        Log.v(LOG_TAG, "playlits total : " + playlist.getTotal());


        viewHolder.itemArtistName.setText(song.getSongArtist());

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
        if (items != null) {
            return items.getSongs().size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private final ImageView itemArtistThumbnail;
        private final TextView itemArtistName;
        private Artist itemArtist;
        private Song song;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            itemArtistThumbnail = (ImageView) view.findViewById(R.id.artist_thumbnail);
            itemArtistName = (TextView) view.findViewById(R.id.artist_name);
        }

        @Override
        public void onClick(View v) {
            //parentActivity.instantiateTopTracksFragment(itemArtist);
            parentActivity.onArtistSelected(itemArtist);
        }

        public void setArtist(Artist artist) {
            itemArtist = artist;
        }
        public void setSong(Song song) {
            song = song;
        }
    }
}
