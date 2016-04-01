package com.xinay.droid.fm;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.SongArtEvent;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.SongArtResponse;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GenresFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {link GenresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenresFragment extends Fragment {

    private final String LOG_TAG = GenresFragment.class.getSimpleName();

//    private List<Song> keys;
//    private Map<String, Song> songs;

    private GenresListAdapter genresListAdapter;
    //    private CardAdapter genresListAdapter;
    private RecyclerView mGenresRecyclerView;

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private Bundle mTmpReenterState;

    private int currentPosition;

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            Log.v(LOG_TAG, "SharedElementCallback - onMapSharedElements() ");

            if (mTmpReenterState != null) {
                int startingPosition = mTmpReenterState.getInt(Constants.EXTRA_STARTING_ALBUM_POSITION);
                int currentPosition = mTmpReenterState.getInt(Constants.EXTRA_CURRENT_ALBUM_POSITION);
                if (startingPosition != currentPosition) {
                    // If startingPosition != currentPosition the user must have swiped to a
                    // different page in the DetailsActivity. We must update the shared element
                    // so that the correct one falls into place.
                    String newTransitionName = key;
                    View newSharedElement = mGenresRecyclerView.findViewWithTag(newTransitionName);
                    if (newSharedElement != null) {
                        names.clear();
                        names.add(newTransitionName);
                        sharedElements.clear();
                        sharedElements.put(newTransitionName, newSharedElement);
                    }
                }

                mTmpReenterState = null;
            }
        }
    };


    //    private OnFragmentInteractionListener mListener;

    public static GenresFragment newInstance(String key) {
        Bundle args = new Bundle();
        //args.putInt(ARG_ALBUM_IMAGE_POSITION, position);
        //args.putInt(ARG_STARTING_ALBUM_IMAGE_POSITION, startingPosition);
        GenresFragment fragment = new GenresFragment();
        fragment.setKey(key);
        fragment.setArguments(args);
        return fragment;
    }

    public GenresFragment() {
        // Required empty public constructor
        //genresListAdapter = new GenresListAdapter();
        Log.v(LOG_TAG, "GenresFragment - new GenresListAdapter() ");
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface OnFragmentInteractionListener {
        /**
         * GenresFragmentCallback for when an item has been selected.
         */
        public void onSongSelected(Song song, ImageView imageView);
    }

    public RecyclerView getmGenresRecyclerView() {
        return mGenresRecyclerView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        RecyclerView.LayoutManager layoutManager = mGenresRecyclerView.getLayoutManager();
        if (layoutManager != null && layoutManager instanceof GridLayoutManager) {
            currentPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        outState.putString(Constants.EXTRA_GENRE_KEY, key);
        outState.putInt(Constants.EXTRA_CURRENT_ALBUM_POSITION, currentPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate : " + this.toString());
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            key = savedInstanceState.getString(Constants.EXTRA_GENRE_KEY);
            currentPosition = savedInstanceState.getInt(Constants.EXTRA_CURRENT_ALBUM_POSITION);
        }
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy : " + this.toString());

        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView : " + this.toString());
        // Inflate the layout for this fragmentr
        View view = inflater.inflate(R.layout.fragment_genres_list, container, false);
        //GridView gridview = (GridView)view.findViewById(R.id.gridview);

        if (savedInstanceState != null) {
            key = savedInstanceState.getString(Constants.EXTRA_GENRE_KEY);
            currentPosition = savedInstanceState.getInt(Constants.EXTRA_CURRENT_ALBUM_POSITION);
        }

        mGenresRecyclerView = (RecyclerView) view.findViewById(R.id.songs_list);
        mGenresRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mGenresRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //List<ItemObject> allItems = getAllItemObject();
        //CustomAdapter customAdapter = new CustomAdapter(getActivity(), allItems);
        //gridview.setAdapter(customAdapter);

        if (genresListAdapter == null) {
            Log.v(LOG_TAG, "new GenresListAdapter...");
            genresListAdapter = new GenresListAdapter((MainActivity) getActivity());
        }
        Log.v(LOG_TAG, "genresListAdapter.setGenre(key) , key: " + key);
        genresListAdapter.setGenre(key);
            genresListAdapter.notifyDataSetChanged();
        Log.v(LOG_TAG, "mGenresRecyclerView.setAdapter...");
        mGenresRecyclerView.setAdapter(genresListAdapter);

        if (savedInstanceState != null) {
            Log.v(LOG_TAG, "scrollToPosition - currentPosition: " + currentPosition);
            mGenresRecyclerView.scrollToPosition(currentPosition);
            int startingPosition = savedInstanceState.getInt(Constants.EXTRA_STARTING_ALBUM_POSITION);
            int currentPosition = savedInstanceState.getInt(Constants.EXTRA_CURRENT_ALBUM_POSITION);
            Log.v(LOG_TAG, "startingPosition: " + startingPosition + " , currentPosition: " + currentPosition);
            if (startingPosition != currentPosition) {
                mGenresRecyclerView.scrollToPosition(currentPosition);
            }
            Log.v(LOG_TAG, "mGenresRecyclerView.getViewTreeObserver().addOnPreDrawListener()");
            mGenresRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    Log.v(LOG_TAG, "onPreDraw()");
                    mGenresRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                    mGenresRecyclerView.requestLayout();
                    Log.v(LOG_TAG, "startPostponedEnterTransition()");
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        Log.v(LOG_TAG, "genresListAdapter.notifyDataSetChanged()...");
        genresListAdapter.notifyDataSetChanged();
    }

    public void setSongs(Map<String, Song> songs) {
        Log.v(LOG_TAG, "setSongs : " + this.toString());

        //this.songs = songs;
        if (genresListAdapter == null) {
            genresListAdapter = new GenresListAdapter((MainActivity) getActivity());
        }
        genresListAdapter.notifyDataSetChanged();
    }


    @Subscribe
    public void onSongArtEvent(SongArtEvent event) {
        //Log.v(LOG_TAG, "onSongArtEvent : " + this.toString());

        SongArtResponse songArtResponse = event.response;
        SongArtResponse.SongArt songArt = songArtResponse.getSongArt();
        //Log.v(LOG_TAG, "onSongArtEvent - song url : " + songArt.getArtUrl());

        String songArtKey = songArtResponse.getKey();

        int position = songArtResponse.getPosition();

        //Log.v(LOG_TAG, "onSongArtEvent - song key : " + songArtKey);
        //Log.v(LOG_TAG, "onSongArtEvent - song position : " + position);
        //Log.v(LOG_TAG, "onSongArtEvent - songArtKey : " + songArtKey);
        //Log.v(LOG_TAG, "onSongArtEvent - songs.size: " + PlayerManager.getInstance().getSongsByGenre(key).size());

        if (PlayerManager.getInstance().getSongsByGenre(key) != null && position < PlayerManager.getInstance().getSongsByGenre(key).size()) {
            Song song = PlayerManager.getInstance().getSongsByGenre(key).get(position);

            //Log.v(LOG_TAG, "onSongArtEvent - song getSongTitle : " + song.getSongTitle());

            if (song.getSongTitle().equals(songArtKey)) {
                if (song != null) {
                    final String albumArtUrl = songArt.getArtUrl();
                    //if (albumArtUrl.indexOf("dar.fm") != -1) albumArtUrl = null;
                    song.setAlbumArtUrl(albumArtUrl);
                    genresListAdapter.notifyDataSetChanged();
                }
            }
        }

        // check for a valid Album Art URL
        /*
        if (albumArtUrl == null) {
            if (artist.indexOf(songArt.getArtist()) != -1) {
                albumArtUrl = songArt.getArtUrl();
                Log.v(LOG_TAG, "bus - unregister: " + this.toString());
                // register with the bus to receive events
                BusProvider.getInstance().unregister(this);
                if (Patterns.WEB_URL.matcher(songArt.getArtUrl()).matches()) {
                    Picasso.with(getActivity())
                            .load(albumArtUrl)
                            .into(mAlbumCover);
                }
            }
        }
        */
    }
}
