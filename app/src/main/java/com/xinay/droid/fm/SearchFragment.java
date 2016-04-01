package com.xinay.droid.fm;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.xinay.droid.fm.R;
import com.xinay.droid.fm.async.RadioStationsClient;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.ArtistSearchEvent;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.model.Artist;
import com.xinay.droid.fm.model.ArtistSearchResponse;
import com.xinay.droid.fm.model.Artists;
import com.xinay.droid.fm.model.Playlist;
import com.xinay.droid.fm.model.PlaylistResponse;
import com.xinay.droid.fm.model.Songs;
import com.xinay.droid.fm.model.Station;
import com.xinay.droid.fm.model.TopSongsResponse;

import org.parceler.Parcels;

import java.util.List;

/**
 * Created by luisvivero on 1/23/16.
 */
public class SearchFragment extends Fragment {

    private final String LOG_TAG = SearchFragment.class.getSimpleName();

    //private static final String KEY_ARTIST_LIST = "artists";
    private static final String KEY_PLAYLIST = "playlist";

    private Playlist playlist;

    private PlaylistResponse playListResponse;
    private RadioStationsClient mRadioClient;

    private SearchResultsListAdapter searchResultsListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Parcelable wrappedPlaylist;
    private RecyclerView mResultsList;
    private ProgressBar mProgressBar;
    private EditText mSearchText;
    private String searchQuery;

    private OnFragmentInteractionListener mListener;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface OnFragmentInteractionListener {
        /**
         * SearchFragmentCallback for when an item has been selected.
         */
        public void onStationSelected(Station station);
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save list state
        //mListState = mLayoutManager.onSaveInstanceState();
        //outState.putParcelable(Constants.SEARCH_RESULTS_INSTANCE_STATE, mListState);

        wrappedPlaylist = Parcels.wrap(Playlist.class, playlist);
        outState.putParcelable(KEY_PLAYLIST, wrappedPlaylist);
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
        if (wrappedPlaylist != null) {
            mLayoutManager.onRestoreInstanceState(wrappedPlaylist);
        } else {
            mLayoutManager = new LinearLayoutManager(getActivity());
        }

        if(savedInstanceState != null) {
            // read the artists list from the saved state
            //mListState = savedInstanceState.getParcelableArrayList(KEY_ARTIST_LIST);
            wrappedPlaylist = savedInstanceState.getParcelable(KEY_PLAYLIST);
            playlist = Parcels.unwrap(wrappedPlaylist);
        }
        mRadioClient = new RadioStationsClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        searchResultsListAdapter = new SearchResultsListAdapter((MainActivity)getActivity());
        if (playlist != null) {
            searchResultsListAdapter.setPlaylist(playlist);
        }

        //searchResultsListAdapter.setItems(mSongs);

        View rootView = inflater.inflate(R.layout.fragment_search_list, container, false);

        //mSearchView = (SearchView) findViewById(R.id.search_query);
        mSearchText = (EditText) rootView.findViewById(R.id.search_edit_text);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.search_progress_bar);
        mResultsList = (RecyclerView) rootView.findViewById(R.id.search_results);
        mResultsList.setHasFixedSize(true);

        if (mResultsList.getLayoutManager() == null) {
            mResultsList.setLayoutManager(mLayoutManager);
        }
        mResultsList.setAdapter(searchResultsListAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.v(LOG_TAG, "actionId: " + actionId);
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchQuery = mSearchText.getText().toString();
                    Log.v(LOG_TAG, "search query: " + searchQuery);
                    searchForArtist(searchQuery);
                    return true;
                }
                return false;
            }
        });

//        if (mArtists == null) {
//            searchForArtist("Michael");
//        }

        return rootView;
    }

    public void onStationSelected(Station station) {
        if (mListener != null) {
            mListener.onStationSelected(station);
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

    @Subscribe
    public void onTopSongsEvent(TopSongsEvent event) {

        TopSongsResponse mSongs = event.response;

        int resultsSize = mSongs.getSongs().size();

        Log.v(LOG_TAG, "onTopSongsEvent - songs size : " + resultsSize);

        mProgressBar.setVisibility(View.GONE);

        if (resultsSize > 0) {
            searchResultsListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.no_results_found), searchQuery), Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.search_results_hint)), Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onArtistSearchEvent(ArtistSearchEvent event){

        ArtistSearchResponse apiResponse = event.response;

        int resultsSize = apiResponse.getArtists().getItems().size();

        Log.v(LOG_TAG, "artists size : " + resultsSize);

        mProgressBar.setVisibility(View.GONE);

        if (resultsSize > 0) {
            searchResultsListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.no_results_found), searchQuery), Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.search_results_hint)), Toast.LENGTH_LONG).show();
        }
    }

    private void searchForArtist(String searchKey) {
        Log.v(LOG_TAG, "searchForArtist: " + searchKey);

        mProgressBar.setVisibility(View.VISIBLE);

        final String theSearchKey = searchKey;

        //RadioStationsClient client = new RadioStationsClient();
        mRadioClient.doTopSongs("Music");
    }


}
