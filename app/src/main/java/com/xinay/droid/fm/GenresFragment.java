package com.xinay.droid.fm;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.SongArtEvent;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.SongArtResponse;
import com.xinay.droid.fm.model.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GenresFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GenresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenresFragment extends Fragment {

    private final String LOG_TAG = GenresFragment.class.getSimpleName();

    private List<Song> keys;
    private Map<String, Song> songs;

    private GenresListAdapter genresListAdapter;
    private RecyclerView mGenresRecyclerView;

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    //    private OnFragmentInteractionListener mListener;

    public GenresFragment() {
        // Required empty public constructor
        //genresListAdapter = new GenresListAdapter();
        Log.v(LOG_TAG, "GenresFragment - new GenresListAdapter() ");
//        genresListAdapter = new GenresListAdapter((MainActivity) getActivity());

        keys = new ArrayList<>();
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
        public void onSongSelected(Song song);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GenresFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static GenresFragment newInstance(String param1, String param2) {
//        GenresFragment fragment = new GenresFragment();
//        Bundle args = new Bundle();
////        args.putString(ARG_PARAM1, param1);
////        args.putString(ARG_PARAM2, param2);
////        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate : " + this.toString());
        super.onCreate(savedInstanceState);
        if (songs == null) {
            Log.v(LOG_TAG, "getSongsByGenre");
            initSongs();
            //PlayerManager.getInstance().getRadioStationsClient().doTopSongs(key);
        }
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy : " + this.toString());

        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    private void initSongs() {
        Log.v(LOG_TAG, "initSongs : " + this.toString());

        List<Song> songs = PlayerManager.getInstance().getSongsByGenre(key);

        Map<String, Song> songMap = new HashMap<>();
        int idx = 0;
        for (Song song : songs) {
            String songKey = this.toString() + idx;
            Log.v(LOG_TAG, "songKey: " + songKey);
            songMap.put(songKey, song);
            idx++;
        }

        this.setSongs(songMap);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView : " + this.toString());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_genres_list, container, false);
        //GridView gridview = (GridView)view.findViewById(R.id.gridview);

        mGenresRecyclerView = (RecyclerView) view.findViewById(R.id.songs_list);
        mGenresRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mGenresRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //List<ItemObject> allItems = getAllItemObject();
        //CustomAdapter customAdapter = new CustomAdapter(getActivity(), allItems);
        //gridview.setAdapter(customAdapter);

        if (genresListAdapter == null) {
            Log.v(LOG_TAG, "new GenresListAdapter...");
            genresListAdapter = new GenresListAdapter((MainActivity) getActivity());
//            genresListAdapter = new GenresListAdapter();
        }
        initSongs();
        if (songs != null) {
            genresListAdapter.setSongs(songs);
            genresListAdapter.notifyDataSetChanged();
        }
        Log.v(LOG_TAG, "mGenresRecyclerView.setAdapter...");
        mGenresRecyclerView.setAdapter(genresListAdapter);

//        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getActivity(), "Position: " + position, Toast.LENGTH_SHORT).show();
//            }
//        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }


    private List<ItemObject> getAllItemObject(){
        List<ItemObject> items = new ArrayList<>();
        items.add(new ItemObject(R.drawable.droid_fm,"Dip It Low", "Christina Milian"));
        items.add(new ItemObject(R.drawable.droid_fm,"Someone like you", "Adele Adkins"));
        items.add(new ItemObject(R.drawable.droid_fm,"Ride", "Ciara"));
        items.add(new ItemObject(R.drawable.droid_fm,"Paparazzi", "Lady Gaga"));
        items.add(new ItemObject(R.drawable.droid_fm,"Forever", "Chris Brown"));
        items.add(new ItemObject(R.drawable.droid_fm,"Stay", "Rihanna"));
        items.add(new ItemObject(R.drawable.droid_fm,"Marry me", "Jason Derulo"));
        items.add(new ItemObject(R.drawable.droid_fm,"Waka Waka", "Shakira"));
        items.add(new ItemObject(R.drawable.droid_fm,"Dark Horse", "Katy Perry"));
        items.add(new ItemObject(R.drawable.droid_fm,"Dip It Low", "Christina Milian"));
        return items;
    }

    public void setSongs(Map<String, Song> songs) {
        Log.v(LOG_TAG, "setSongs : " + this.toString());

        this.songs = songs;
        if (genresListAdapter == null) {
            genresListAdapter = new GenresListAdapter((MainActivity) getActivity());
//            genresListAdapter = new GenresListAdapter();
        }
        genresListAdapter.setSongs(this.songs );
        genresListAdapter.notifyDataSetChanged();
    }

    public Map<String, Song> getSongs() {
        return songs;
    }

    @Subscribe
    public void onSongArtEvent(SongArtEvent event) {
        Log.v(LOG_TAG, "onSongArtEvent : " + this.toString());

        SongArtResponse songArtResponse = event.response;
        SongArtResponse.SongArt songArt = songArtResponse.getSongArt();
        Log.v(LOG_TAG, "onSongArtEvent - song url : " + songArt.getArtUrl());

        String key = songArtResponse.getKey();

        Log.v(LOG_TAG, "onSongArtEvent - song key : " + key);

        if (songs != null) {
            Song song = songs.get(key);

            if (song != null) {
                final String albumArtUrl = songArt.getArtUrl();
                //if (albumArtUrl.indexOf("dar.fm") != -1) albumArtUrl = null;
                song.setAlbumArtUrl(albumArtUrl);
                genresListAdapter.notifyDataSetChanged();
            }
        }

        // check for a valid Album Art URL
//        if (albumArtUrl == null) {
//            if (artist.indexOf(songArt.getArtist()) != -1) {
//                albumArtUrl = songArt.getArtUrl();
//                Log.v(LOG_TAG, "bus - unregister: " + this.toString());
//                // register with the bus to receive events
//                BusProvider.getInstance().unregister(this);
//                if (Patterns.WEB_URL.matcher(songArt.getArtUrl()).matches()) {
//                    Picasso.with(getActivity())
//                            .load(albumArtUrl)
//                            .into(mAlbumCover);
//                }
//            }
//        }
    }
}
