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
//            } else {
//                // If mTmpReenterState is null, then the activity is exiting.
//                View navigationBar = findViewById(android.R.id.navigationBarBackground);
//                View statusBar = findViewById(android.R.id.statusBarBackground);
//                if (navigationBar != null) {
//                    names.add(navigationBar.getTransitionName());
//                    sharedElements.put(navigationBar.getTransitionName(), navigationBar);
//                }
//                if (statusBar != null) {
//                    names.add(statusBar.getTransitionName());
//                    sharedElements.put(statusBar.getTransitionName(), statusBar);
//                }
            }
        }
    };


    //    private OnFragmentInteractionListener mListener;

    public static GenresFragment newInstance(String key) {
        Bundle args = new Bundle();
//        args.putInt(ARG_ALBUM_IMAGE_POSITION, position);
//        args.putInt(ARG_STARTING_ALBUM_IMAGE_POSITION, startingPosition);
        GenresFragment fragment = new GenresFragment();
        fragment.setKey(key);
        fragment.setArguments(args);
        return fragment;
    }

    public GenresFragment() {
        // Required empty public constructor
        //genresListAdapter = new GenresListAdapter();
        Log.v(LOG_TAG, "GenresFragment - new GenresListAdapter() ");
//        genresListAdapter = new GenresListAdapter((MainActivity) getActivity());

//        keys = new ArrayList<>();
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * param param1 Parameter 1.
     * param param2 Parameter 2.
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


    public RecyclerView getmGenresRecyclerView() {
        return mGenresRecyclerView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate : " + this.toString());
        super.onCreate(savedInstanceState);

//        Log.v(LOG_TAG, "setExitSharedElementCallback(mCallback)");
//        setExitSharedElementCallback(mCallback);

//        if (PlayerManager.getInstance().getSongsByGenre(key) == null) {
//            PlayerManager.getInstance().getSongsByGenre(key);
//        }

//        if (songs == null) {
//            Log.v(LOG_TAG, "getSongsByGenre");
//            initSongs();
//            //PlayerManager.getInstance().getRadioStationsClient().doTopSongs(key);
//        }
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

/*
    private void initSongs() {
        Log.v(LOG_TAG, "initSongs : " + this.toString());
        List<Song> songs = PlayerManager.getInstance().getSongsByGenre(key);
        if (songs != null) {
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
    }
*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView : " + this.toString());
        // Inflate the layout for this fragmentr
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
//            genresListAdapter = new CardAdapter();
            genresListAdapter = new GenresListAdapter((MainActivity) getActivity());
//            genresListAdapter = new GenresListAdapter();
        }
//        initSongs();
//        if (PlayerManager.getInstance().getSongsByGenre(key) != null) {
//            genresListAdapter.setSongs(songs);
        genresListAdapter.setGenre(key);
            genresListAdapter.notifyDataSetChanged();
//        }
        Log.v(LOG_TAG, "mGenresRecyclerView.setAdapter...");
        mGenresRecyclerView.setAdapter(genresListAdapter);

//        genresListAdapter.setSongs(songs);
//        mGenresRecyclerView.setAdapter(genresListAdapter);

        //        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
//                // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
//                mRecyclerView.requestLayout();
//                startPostponedEnterTransition();
//                return true;
//            }
//        });

        if (savedInstanceState!=null) {
            int startingPosition = savedInstanceState.getInt(Constants.EXTRA_STARTING_ALBUM_POSITION);
            int currentPosition = savedInstanceState.getInt(Constants.EXTRA_CURRENT_ALBUM_POSITION);
            if (startingPosition != currentPosition) {
                mGenresRecyclerView.scrollToPosition(currentPosition);
            }
//            Log.v(LOG_TAG, "mGenresRecyclerView.getViewTreeObserver().addOnPreDrawListener()");
//            mGenresRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//                    Log.v(LOG_TAG, "onPreDraw()");
//                    mGenresRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
//                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
//                    mGenresRecyclerView.requestLayout();
//                    Log.v(LOG_TAG, "startPostponedEnterTransition()");
//                    getActivity().startPostponedEnterTransition();
//                    return true;
//                }
//            });
        }

//        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getActivity(), "Position: " + position, Toast.LENGTH_SHORT).show();
//            }
//        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
        Log.v(LOG_TAG, "genresListAdapter.notifyDataSetChanged()...");
        genresListAdapter.notifyDataSetChanged();
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


/*    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String imageTransitionName = "";
        String textTransitionName = "";

        ImageView imageView = (ImageView) view.findViewById(R.id.album_art);

        //TextView textView = (TextView) view.findViewById(R.id.textView);

        //ImageView staticImage = (ImageView) getView().findViewById(R.id.imageView);

        PlayerFragment endFragment = new PlayerFragment();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementReturnTransition(TransitionInflater.from(
                    getActivity()).inflateTransition(R.transition.change_image_trans));
            setExitTransition(TransitionInflater.from(
                    getActivity()).inflateTransition(android.R.transition.fade));

            endFragment.setSharedElementEnterTransition(TransitionInflater.from(
                    getActivity()).inflateTransition(R.transition.change_image_trans));
            endFragment.setEnterTransition(TransitionInflater.from(
                    getActivity()).inflateTransition(android.R.transition.fade));

            imageTransitionName = imageView.getTransitionName();
            //textTransitionName = textView.getTransitionName();

            // Create new fragment to add (Fragment B)
            Fragment fragment = PlayerFragment.newInstance();
            fragment.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.change_image_trans));
            fragment.setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.explode));

            // Our shared element (in Fragment A)
            //mProductImage   = (ImageView) mLayout.findViewById(R.id.product_detail_image);

            // Add Fragment B
            FragmentTransaction ft = getFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack("transaction")
                    .addSharedElement(imageView, "MyTransition");
            ft.commit();


        }

        Bundle bundle = new Bundle();
        bundle.putString("TRANS_NAME", imageTransitionName);
        //bundle.putString("ACTION", textView.getText().toString());
        //bundle.putString("TRANS_TEXT", textTransitionName);
        bundle.putParcelable("IMAGE", ((BitmapDrawable) imageView.getDrawable()).getBitmap());
        endFragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, endFragment)
                .addToBackStack("Payment")
                .addSharedElement(imageView, imageTransitionName)
                //.addSharedElement(textView, textTransitionName)
                //.addSharedElement(staticImage, getString(R.string.fragment_image_trans))
                .commit();
    }*/


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

/*
    private class CardAdapter extends RecyclerView.Adapter<CardHolder> {
        private final LayoutInflater mInflater;
        private Map<String, Song> songs;
        private List<String> keys;

        public CardAdapter() {
            mInflater = LayoutInflater.from(getActivity());
        }

        public void setSongs(Map<String, Song> songs) {
            Log.v(LOG_TAG, "setTracks www - songs.size()=" + songs.size());
            this.songs = songs;
            keys = new ArrayList<>();
            keys.addAll(songs.keySet());
        }

        @Override
        public CardHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new CardHolder(mInflater.inflate(R.layout.card_song, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(CardHolder holder, int position) {
            Log.v(LOG_TAG, "onBindViewHolder");
            if (songs == null || holder == null) {
                return;
            }

            //String key = keys.get(position);
            Log.v(LOG_TAG, "song key " + key);

            Song song = songs.get(key);
            if (song != null) {
                Log.v(LOG_TAG, "song title: '" + song.getSongTitle() + "'");
                holder.setSong(song);
                if (song.getAlbumArtUrl() != null) {
                    holder.bind(position);
                    // substitute default image from dar.fm with droid.fm logo
                    if (song.getAlbumArtUrl().indexOf("dar.fm") != -1) {
                        holder.mAlbumImage.setImageResource(R.drawable.droid_fm);
                    } else {
                        Picasso.with(getActivity()).setIndicatorsEnabled(true);
                        Picasso.with(getActivity())
                                .load(song.getAlbumArtUrl())
                                .placeholder(R.drawable.droid_fm)
                                .error(R.drawable.droid_fm)
                                .fit()
                                .into(holder.mAlbumImage);
                    }
                } else {
                    PlayerManager.getInstance().getRadioStationsClient().doSongArt(
                            song.getSongArtist(),
                            song.getSongTitle(),
                            Constants.ALBUM_ART_IMAGE_RESOLUTION,
                            key
                    );
                }
            }

        }

        @Override
        public int getItemCount() {
            if (songs != null) {
                return songs.size();
            } else {
                return 0;
            }
        }
    }

    private class CardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView mAlbumImage;
        private final TextView itemSongTitle;
        private final TextView itemArtistName;
        private final TextView itemStationCallSign;
        private Song song;
        private int mAlbumPosition;
        private boolean cardClicked = false;

        public CardHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mAlbumImage = (ImageView) itemView.findViewById(R.id.album_art);
            itemSongTitle = (TextView) itemView.findViewById(R.id.song_title);
            itemArtistName = (TextView) itemView.findViewById(R.id.artist_name);
            itemStationCallSign = (TextView) itemView.findViewById(R.id.station_call_sign);

        }

        public void setSong(Song song) {
            this.song = song;
        }

        public void bind(int position) {
            if (song != null) {
                Log.v(LOG_TAG, "bind , mAlbumImage.setTransitionName: " + song.getUberUrl().getUrl());
                Picasso.with(getActivity()).load(song.getUberUrl().getUrl()).into(mAlbumImage);
                mAlbumImage.setTransitionName(song.getUberUrl().getUrl());
                mAlbumImage.setTag(song.getUberUrl().getUrl());
            }
            mAlbumPosition = position;
        }

        @Override
        public void onClick(View v) {
            //parentActivity.instantiateTopTracksFragment(itemArtist);
            //parentActivity.onSongSelected(song, itemAlbumArt);

            // to prevent user from double clicking and start activity twice
            if (!cardClicked) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra(EXTRA_STARTING_ALBUM_POSITION, mAlbumPosition);

                Log.v(LOG_TAG, "onClick , mAlbumImage.getTransitionName(): " + mAlbumImage.getTransitionName());

                getActivity().startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                        mAlbumImage, mAlbumImage.getTransitionName()).toBundle());
                cardClicked = true;
            }
        }
    }*/

    public void setSongs(Map<String, Song> songs) {
        Log.v(LOG_TAG, "setSongs : " + this.toString());

//        this.songs = songs;
        if (genresListAdapter == null) {
//            genresListAdapter = new CardAdapter();
            genresListAdapter = new GenresListAdapter((MainActivity) getActivity());
//            genresListAdapter = new GenresListAdapter();
        }
//        genresListAdapter.setSongs(this.songs );
        genresListAdapter.notifyDataSetChanged();
    }

/*
    public Map<String, Song> getSongs() {
        return songs;
    }
*/

    @Subscribe
    public void onSongArtEvent(SongArtEvent event) {
        Log.v(LOG_TAG, "onSongArtEvent : " + this.toString());

        SongArtResponse songArtResponse = event.response;
        SongArtResponse.SongArt songArt = songArtResponse.getSongArt();
        Log.v(LOG_TAG, "onSongArtEvent - song url : " + songArt.getArtUrl());

        String songArtKey = songArtResponse.getKey();

        int position = songArtResponse.getPosition();

//                Log.v(LOG_TAG, "onSongArtEvent - song key : " + songArtKey);
        Log.v(LOG_TAG, "onSongArtEvent - song position : " + position);
        Log.v(LOG_TAG, "onSongArtEvent - songArtKey : " + songArtKey);
        Log.v(LOG_TAG, "onSongArtEvent - songs.size: " + PlayerManager.getInstance().getSongsByGenre(key).size());

        if (PlayerManager.getInstance().getSongsByGenre(key) != null && position < PlayerManager.getInstance().getSongsByGenre(key).size()) {
            Song song = PlayerManager.getInstance().getSongsByGenre(key).get(position);

            Log.v(LOG_TAG, "onSongArtEvent - song getSongTitle : " + song.getSongTitle());

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
