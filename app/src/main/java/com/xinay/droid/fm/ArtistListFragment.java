package com.xinay.droid.fm;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
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
import com.xinay.droid.R;
import com.xinay.droid.fm.async.RadioStationsClient;
import com.xinay.droid.fm.async.SpotifyClient;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.ArtistSearchEvent;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.model.Artist;
import com.xinay.droid.fm.model.ArtistSearchResponse;
import com.xinay.droid.fm.model.Artists;
import com.xinay.droid.fm.model.Songs;
import com.xinay.droid.fm.model.TopSongsResponse;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArtistListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArtistListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistListFragment extends Fragment {

    private final String LOG_TAG = ArtistListFragment.class.getSimpleName();

    //private static final String KEY_ARTIST_LIST = "artists";
    private static final String KEY_SONGS_LIST = "songs";

    private Artists mArtists;
    private SpotifyClient mSpotifyClient;

    private TopSongsResponse mSongs;
    private RadioStationsClient mRadioClient;

    private SearchResultsListAdapter searchResultsListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Parcelable mListState;
    private RecyclerView mResultsList;
    private ProgressBar mProgressBar;
    private EditText mSearchText;
    private String searchQuery;

    private OnFragmentInteractionListener mListener;

    public static ArtistListFragment newInstance() {
        return new ArtistListFragment();
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
        public void onArtistSelected(Artist artist);
    }

    public ArtistListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save list state
        //mListState = mLayoutManager.onSaveInstanceState();
        //outState.putParcelable(Constants.SEARCH_RESULTS_INSTANCE_STATE, mListState);

        mListState = Parcels.wrap(Songs.class, mSongs);
        outState.putParcelable(KEY_SONGS_LIST, mListState);
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

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        // Retrieve list state and list/item positions
//        //mListState = savedInstanceState.getParcelable(Constants.SEARCH_RESULTS_INSTANCE_STATE);
//        mListState = savedInstanceState.getParcelable(KEY_ARTIST_LIST);
//        mArtists = Parcels.unwrap(mListState);
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        } else {
            mLayoutManager = new LinearLayoutManager(getActivity());
        }

        if(savedInstanceState != null) {
            // read the artists list from the saved state
            //mListState = savedInstanceState.getParcelableArrayList(KEY_ARTIST_LIST);
            mListState = savedInstanceState.getParcelable(KEY_SONGS_LIST);
            mSongs = Parcels.unwrap(mListState);
        }
        //mSpotifyClient = new SpotifyClient();
        mRadioClient = new RadioStationsClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        searchResultsListAdapter = new SearchResultsListAdapter((MainActivity)getActivity());
        if (mArtists != null) {
            searchResultsListAdapter.setArtists(mArtists);
        }

        searchResultsListAdapter.setItems(mSongs);

        View rootView = inflater.inflate(R.layout.fragment_artist_list, container, false);

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

        if (mArtists == null) {
            searchForArtist("Michael");
        }

        return rootView;
    }

    public void onArtistSelected(Artist artist) {
        if (mListener != null) {
            mListener.onArtistSelected(artist);
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
            searchResultsListAdapter.setItems(mSongs);
            //searchResultsListAdapter.setArtists(mArtists);
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
            mArtists = apiResponse.getArtists();
            searchResultsListAdapter.setArtists(mArtists);
            searchResultsListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.no_results_found), searchQuery), Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.search_results_hint)), Toast.LENGTH_LONG).show();
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

    private void searchForArtist(String searchKey) {
        Log.v(LOG_TAG, "searchForArtist: " + searchKey);

        mProgressBar.setVisibility(View.VISIBLE);

        final String theSearchKey = searchKey;

//        mSpotifyClient.doArtistSearch(searchKey);

        //RadioStationsClient client = new RadioStationsClient();
        mRadioClient.doTopSongs("Music");

        /*
        ServiceHelper.get().search(searchKey, Constants.TYPE_ARTISTS, new Callback<ArtistSearchResponse>() {

            @Override
            public void success(ArtistSearchResponse apiResponse, Response response) {
                Log.v(LOG_TAG, "success");

                int resultsSize = apiResponse.getArtists().getItems().size();

                Log.v(LOG_TAG, "artists size : " + resultsSize);

                mProgressBar.setVisibility(View.GONE);

                if (resultsSize > 0) {
                    mArtists = apiResponse.getArtists();
                    searchResultsListAdapter.setArtists(mArtists);
                    searchResultsListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), String.format(getResources().getString(R.string.no_results_found), theSearchKey), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getActivity(), String.format(getResources().getString(R.string.search_results_hint)), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v(LOG_TAG, "failure");
                mProgressBar.setVisibility(View.GONE);
                Log.v(LOG_TAG, "error message: " + error.getMessage());
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        */
    }
}
