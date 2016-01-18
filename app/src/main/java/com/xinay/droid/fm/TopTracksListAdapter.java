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
import com.xinay.droid.fm.model.Album;
import com.xinay.droid.fm.model.Image;
import com.xinay.droid.fm.model.Track;

import java.util.List;

/**
 * Created by luisvivero on 7/13/15.
 */
public class TopTracksListAdapter extends RecyclerView.Adapter<TopTracksListAdapter.ViewHolder>
{

    private final String LOG_TAG = TopTracksListAdapter.class.getSimpleName();

    private static MainActivity parentActivity;
    private List<Track> tracks;

    public TopTracksListAdapter(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }


    @Override
    public int getItemCount() {
        if (tracks != null) {
            return tracks.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder");
        if (tracks == null) {
            return;
        }

        Track track = tracks.get(position);
        track.setIndex(position);

        Log.v(LOG_TAG, "track name " + track.getName());

        holder.setTrack(track);
        Log.v(LOG_TAG, "set Track : " + track.getId());

        holder.itemTrackTitle.setText(track.getName());

        Album album = track.getAlbum();

        Log.v(LOG_TAG, "album name " + album.getName());
        holder.itemAlbumName.setText(album.getName());

        Log.v(LOG_TAG, "are images null : "  + String.valueOf(album.getImages() == null));

        if (album.getImages() != null && album.getImages().size() > 0) {

            Log.v(LOG_TAG, "get first image");

            Image image = album.getImages().get(0);

            // check for a valid Album Art URL
            if (Patterns.WEB_URL.matcher(image.getUrl()).matches()) {
                Picasso.with(parentActivity.getApplicationContext())
                        .load(image.getUrl())
                        .into(holder.itemAlbumThumbnail);
            } else {
                // Set a default image if thumbnail not found or not valid
                holder.itemAlbumThumbnail.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.v(LOG_TAG, "onCreateViewHolder");
        LayoutInflater inflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item_track, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private final ImageView itemAlbumThumbnail;
        private final TextView itemTrackTitle;
        private final TextView itemAlbumName;
        private Track itemTrack;
        private int position;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);

            itemAlbumThumbnail = (ImageView) view.findViewById(R.id.album_thumbnail);
            itemTrackTitle = (TextView) view.findViewById(R.id.track_title);
            itemAlbumName = (TextView) view.findViewById(R.id.album_name);
        }

        @Override
        public void onClick(View v) {
            //parentActivity.instantiatePlayerFragment(itemTrack);
            parentActivity.onTrackSelected(itemTrack);
        }

        public void setTrack(Track track) {
            itemTrack = track;
        }
    }
}
