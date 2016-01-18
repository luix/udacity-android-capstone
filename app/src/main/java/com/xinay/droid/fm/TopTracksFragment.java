package com.xinay.droid.fm;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.xinay.droid.R;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.TopTracksEvent;
import com.xinay.droid.fm.model.Album;
import com.xinay.droid.fm.model.Image;
import com.xinay.droid.fm.model.TopTracksResponse;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.util.Constants;

import org.parceler.Parcels;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopTracksFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopTracksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopTracksFragment extends Fragment {

    private final String LOG_TAG = TopTracksFragment.class.getSimpleName();

    private static final String KEY_TRACKS_LIST = "tracks";

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mResultsList;
    private Parcelable mListState;
    private ProgressBar mProgressBar;
    private TopTracksListAdapter topTracksListAdapter;
    private String artistId;
    private String artistName;
    private TopTracksResponse mTracks;
    //private Tracks mTracks;

    private OnFragmentInteractionListener mListener;

    public static TopTracksFragment newInstance(String artistId, String artistName) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARTIST_ID_KEY, artistId);
        args.putString(Constants.ARTIST_NAME_KEY, artistName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface OnFragmentInteractionListener {
        /**
         * ArtistListFragmentCallback for when an item has been selected.
         */
        public void onTrackSelected(Track track);
    }

    public TopTracksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save list state
        //mListState = mLayoutManager.onSaveInstanceState();
        //outState.putParcelable(Constants.SEARCH_RESULTS_INSTANCE_STATE, mListState);

        mListState = Parcels.wrap(TopTracksResponse.class, mTracks);
        outState.putParcelable(KEY_TRACKS_LIST, mListState);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Bundle extras = getIntent().getExtras();
            artistId = getArguments().getString(Constants.ARTIST_ID_KEY);
            artistName = getArguments().getString(Constants.ARTIST_NAME_KEY);
        }
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        } else {
            mLayoutManager = new LinearLayoutManager(getActivity());
        }
        if(savedInstanceState != null) {
            // read top tracks list from the saved state
            mListState = savedInstanceState.getParcelable(KEY_TRACKS_LIST);
            mTracks = Parcels.unwrap(mListState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        topTracksListAdapter = new TopTracksListAdapter((MainActivity)getActivity());
        if (mTracks != null) {
            topTracksListAdapter.setTracks(mTracks.getTracks());
        }

//        ((MainActivity)getActivity()).getActionBar().setTitle(getResources().getString(R.string.top_tracks_title));
//        ((MainActivity)getActivity()).getActionBar().setSubtitle(artistName);
//        getActivity().getActionBar().setTitle(getResources().getString(R.string.top_tracks_title));
//        getActivity().getActionBar().setSubtitle(artistName);

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.top_tracks_progress_bar);

        if(savedInstanceState != null) {
            // read the tracks list from the saved state
            mListState = savedInstanceState.getParcelable(KEY_TRACKS_LIST);
            mTracks = Parcels.unwrap(mListState);
            topTracksListAdapter.setTracks(mTracks.getTracks());
        }

        mResultsList = (RecyclerView) rootView.findViewById(R.id.top_tracks_results);
        mResultsList.setHasFixedSize(true);
        mResultsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mResultsList.setAdapter(topTracksListAdapter);

        getTopTracksByArtist();

        // Inflate the layout for this fragment
        return rootView;
    }

    public void onButtonPressed(Track track) {
        if (mListener != null) {
            mListener.onTrackSelected(track);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    protected void instantiatePlayerFragment(Track track) {
        Log.v(LOG_TAG, "instantiatePlayerFragment");

        Log.v(LOG_TAG, "put extra: track id=" + track.getId());
        Log.v(LOG_TAG, "put extra: track name=" + track.getName());
        Log.v(LOG_TAG, "put extra: track href=" + track.getHref());
        Log.v(LOG_TAG, "put extra: track uri=" + track.getUri());
        Log.v(LOG_TAG, "put extra: track preview=" + track.getPreviewUrl());
        Log.v(LOG_TAG, "put extra: track album=" + track.getAlbum());

//        Intent intent = new Intent(this, PlayerActivity.class)
//                .putExtra(Constants.ARTIST_NAME_KEY, artistName)
//                .putExtra(Constants.TRACK_ID_KEY, (Serializable) track);
//        startActivity(intent);
    }

    @Subscribe
    public void onTopTracksEvent(TopTracksEvent event){

        TopTracksResponse apiResponse = event.response;

        int resultsSize = apiResponse.getTracks().size();

        Log.v(LOG_TAG, "tracks size : " + resultsSize);

        mProgressBar.setVisibility(View.GONE);

        if (resultsSize > 0) {
            mTracks = new TopTracksResponse();
            mTracks.setTracks(apiResponse.getTracks());
            //((MainActivity)getActivity()).setTracks(apiResponse.getTracks());
            for (Track track : mTracks.getTracks()) {
                Log.v(LOG_TAG, "track name: " + track.getName());
                Album album = track.getAlbum();
                for (Image image : album.getImages()) {
                    Log.v(LOG_TAG, "image url: " + image.getUrl());
                }
            }
            topTracksListAdapter.setTracks(apiResponse.getTracks());
            topTracksListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.no_tracks_found)), Toast.LENGTH_LONG).show();
        }
//        mResultsFragment = ResultsFragment.newInstance();
//        mResultsFragment.setImageData(ImageDataUtility.getImageDataArrayList(event.result));
//        FragmentTransaction transaction = mFragmentManager.beginTransaction();
//        transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
//        transaction.remove(mConfigFragment);
//        transaction.remove(mSearchFragment);
//        transaction.replace(R.id.main_container, mResultsFragment, "FRAG_RESULTS");
//        transaction.addToBackStack("FRAG_RESULTS");
//        transaction.commit();
    }

    private void getTopTracksByArtist() {
        mProgressBar.setVisibility(View.VISIBLE);
    }
}
