package com.xinay.droid.fm;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.xinay.droid.fm.async.RadioStationsClient;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.SongArtEvent;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.SongArtResponse;
import com.xinay.droid.fm.model.TopSongsResponse;
import com.xinay.droid.fm.transition.TransitionListenerAdapter;
import com.xinay.droid.fm.util.Constants;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment {

    private final String LOG_TAG = PlayerFragment.class.getSimpleName();

    private static final String ARG_TRACK = "track";
    private static final String ARG_ARTIST_NAME = "artist_name";

    private static final int PREVIEW_SONG_DURATION = 30041;
    private static final int UPDATE_DELAY = 1250;
    private static final int UPDATE_FREQUENCY = 500;

    private TextView mSongTitle;
    private TextView mArtistName;
    private TextView mStationId;
    //    private TextView mAlbumName;
    private ImageView mAlbumCover;
    //    private ImageButton mPrevButton;
    private ImageButton mPlayPauseButton;

    private boolean isMovingSeekBar = false;

    private final Handler handler = new Handler();

    private final Runnable updatePositionRunnable = new Runnable() {
        public void run() {
            updatePosition();
        }
    };

    private OnFragmentInteractionListener mListener;

    // reference to the PlayerManager instance
    private PlayerManager playerManager;
    // reference to the RadioStationsClient to make API calls to Radio Stations Web Services
    private RadioStationsClient radioStationsClient;
    // reference to the Current Song playing, paused or ready to start
    private String mGenre;
    private Song mSong;

//    private String artist;
    private String albumArtUrl;

    private static final String ARG_GENRE_KEY = "arg_genre_key";
    private static final String ARG_ALBUM_IMAGE_POSITION = "arg_album_image_position";
    private static final String ARG_STARTING_ALBUM_IMAGE_POSITION = "arg_starting_album_image_position";

    private final Callback mImageCallback = new Callback() {
        @Override
        public void onSuccess() {
            startPostponedEnterTransition();
        }

        @Override
        public void onError() {
            startPostponedEnterTransition();
        }
    };


    private ImageView mAlbumImage;
    private int mStartingPosition;
    private int mAlbumPosition;
    private boolean mIsTransitioning;
    private long mBackgroundImageFadeMillis;

    public static PlayerFragment newInstance(String genre, int position, int startingPosition) {
        Bundle args = new Bundle();
        args.putString(ARG_GENRE_KEY, genre);
        args.putInt(ARG_ALBUM_IMAGE_POSITION, position);
        args.putInt(ARG_STARTING_ALBUM_IMAGE_POSITION, startingPosition);
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        mGenre = getArguments().getString(ARG_GENRE_KEY);
        mStartingPosition = getArguments().getInt(ARG_STARTING_ALBUM_IMAGE_POSITION);
        mAlbumPosition = getArguments().getInt(ARG_ALBUM_IMAGE_POSITION);
        mIsTransitioning = savedInstanceState == null && mStartingPosition == mAlbumPosition;
        mBackgroundImageFadeMillis = 1000;

        Log.v(LOG_TAG, "mStartingPosition: " + mStartingPosition);
        Log.v(LOG_TAG, "mAlbumPosition: " + mAlbumPosition);
        Log.v(LOG_TAG, "mIsTransitioning: " + mIsTransitioning);

        mSong = PlayerManager.getInstance().getSongsByGenre(mGenre).get(mAlbumPosition);

        if (mSong != null) {
            Log.v(LOG_TAG, "mSong title: " + mSong.getSongTitle());
            this.setSong(mSong);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.player_layout, container, false);

        mAlbumCover = (ImageView) rootView.findViewById(R.id.album_art);
        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause_button);
        mSongTitle = (TextView) rootView.findViewById(R.id.song_title);
        mArtistName = (TextView) rootView.findViewById(R.id.artist_name);
        mStationId = (TextView) rootView.findViewById(R.id.station_id);

        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Log.v(LOG_TAG, "albumArtUrl: " + albumArtUrl);
        if (albumArtUrl != null) {
            if (albumArtUrl.indexOf("dar.fm") != -1) {
                mAlbumCover.setImageResource(R.drawable.droid_fm);
            } else {
                Picasso.with(getActivity()).setIndicatorsEnabled(true);
                Picasso.with(getActivity())
                        .load(albumArtUrl)
                        .placeholder(R.drawable.droid_fm)
                        .error(R.drawable.droid_fm)
                        .fit()
                        .into(mAlbumCover);
            }
        } else {
            if (mSong != null) {

//                String key = getParentFragment().toString();
                String key = "6125348712";
                playerManager.getRadioStationsClient().doSongArt(
                        mSong.getSongArtist(),
                        mSong.getSongTitle(),
                        Constants.ALBUM_ART_IMAGE_RESOLUTION,
                        mAlbumPosition
                );
                try {
                    Log.v(LOG_TAG, "bus - register: " + this.toString());
                    // register with the bus to receive events
                    BusProvider.getInstance().register(this);
                } catch (IllegalArgumentException e) {
                    BusProvider.getInstance().unregister(this);
                }
            }
        }

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause(mSong);
                //skip();
            }
        });

        Log.v(LOG_TAG, "mSong: " + mSong);
        if (mSong != null) {
            mSongTitle.setText(mSong.getSongTitle());
            mArtistName.setText(mSong.getSongArtist());
            mStationId.setText(mSong.getCallSign());

            // Add Content Descriptions for Accessibility
            mSongTitle.setContentDescription(mSong.getSongTitle());
            mArtistName.setContentDescription(mSong.getSongArtist());
            mStationId.setContentDescription(mSong.getCallSign());
        }

        mAlbumImage = (ImageView) rootView.findViewById(R.id.album_art);

        //View textContainer = rootView.findViewById(R.id.details_text_container);
        TextView albumTitleText = (TextView) rootView.findViewById(R.id.song_title);

        String albumImageUrl = mSong != null ? mSong.getAlbumArtUrl() : "http://i.imgur.com/wcMIc6s.jpg";
        String albumName = mSong != null ? mSong.getSongTitle() : "The King of Limbs";


        albumTitleText.setText(albumName);

        if (mSong != null) {
            Log.v(LOG_TAG, "mAlbumImage.setTransitionName: " + mSong.getUberUrl().getUrl());
            mAlbumImage.setTransitionName(mSong.getUberUrl().getUrl());
        }

        RequestCreator albumImageRequest = Picasso.with(getActivity()).load(albumImageUrl);

        if (mIsTransitioning) {
            albumImageRequest.noFade();
            getActivity().getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
                @Override
                public void onTransitionEnd(Transition transition) {
                //backgroundImage.animate().setDuration(mBackgroundImageFadeMillis).alpha(1f);
                }
            });
        }

        albumImageRequest.into(mAlbumImage, mImageCallback);
        //backgroundImageRequest.into(backgroundImage);

        return rootView;
    }

    private void startPostponedEnterTransition() {
        if (mAlbumPosition == mStartingPosition) {
            mAlbumImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mAlbumImage.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    /**
     * Returns the shared element that should be transitioned back to the previous Activity,
     * or null if the view is not visible on the screen.
     */
    @Nullable
    ImageView getAlbumImage() {
        if (isViewInBounds(getActivity().getWindow().getDecorView(), mAlbumImage)) {
            return mAlbumImage;
        }
        return null;
    }

    /**
     * Returns true if {@param view} is contained within {@param container}'s bounds.
     */
    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }



    public PlayerFragment() {
        // Required empty public constructor
        playerManager = PlayerManager.getInstance();
    }

    public void setSong(Song asong) {
        this.mSong = asong;
        //Log.v(LOG_TAG, "set mSong - artist: " + mSong.getSongArtist());
        //Log.v(LOG_TAG, "set mSong - title: " + mSong.getSongTitle());
        //Log.v(LOG_TAG, "set mSong - call sign: " + mSong.getCallSign());
        //Log.v(LOG_TAG, "set mSong - station id: " + mSong.getStationId());
        //Log.v(LOG_TAG, "set mSong - albumArtUrl: " + albumArtUrl);
    }

    public interface OnFragmentInteractionListener {
        public void onPlayerPlayPause();
        public void onPreparePlayer(Song song);
        public boolean isPlaying();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save list state
        //mListState = mLayoutManager.onSaveInstanceState();
        //outState.putParcelable(Constants.SEARCH_RESULTS_INSTANCE_STATE, mListState);
        //Parcelable wrapped = Parcels.wrap(mTrack);
        //outState.putParcelable(ARG_TRACK, wrapped);
        //outState.putString(ARG_ARTIST_NAME, artistName);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void playPause(Song song) {
        Log.v(LOG_TAG, "playPause()");
        if (mListener != null) {
            mListener.onPreparePlayer(song);
            //mListener.onPlayerPlayPause();
            if (mListener.isPlaying()) {
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }

    private void updatePosition(){
        handler.removeCallbacks(updatePositionRunnable);
        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }

    private void updateAlbumArt() {

        if (mSong == null) {
            Log.v(LOG_TAG, "updateAlbumArt , mSong is null");
            mSong = playerManager.getCurrentSong();
        }

        //Log.v(LOG_TAG, "updateAlbumArt , mSong - title: " + mSong.getSongTitle());
    }

    @Subscribe
    public void onSongArtEvent(SongArtEvent event) {
        SongArtResponse songArtResponse = event.response;
        SongArtResponse.SongArt songArt = songArtResponse.getSongArt();
        Log.v(LOG_TAG, "onSongArtEvent - mSong url : " + songArt.getArtUrl());

        int position = songArtResponse.getPosition();
        // check for a valid Album Art URL
        if (albumArtUrl == null) {
            if (mSong.getSongArtist().indexOf(songArt.getArtist()) != -1) {
                albumArtUrl = songArt.getArtUrl();
                Log.v(LOG_TAG, "bus - unregister: " + this.toString());
                // unregister with the bus to receive events
                BusProvider.getInstance().unregister(this);
                if (Patterns.WEB_URL.matcher(songArt.getArtUrl()).matches()) {
                    Picasso.with(getActivity())
                            .load(albumArtUrl)
                            .into(mAlbumCover);
                }
            }
        }
    }

    private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isMovingSeekBar = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isMovingSeekBar = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            Log.i("OnSeekBarChangeListener","onProgressChanged");
            if(isMovingSeekBar)
            {
                if (mListener != null) {
                    //mListener.onPlayerScrub(progress);
                }
            }
        }
    };

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
        Log.v(LOG_TAG, "onTopSongsEvent...");

        TopSongsResponse topSongsResponse = event.response;

        int resultsSize = topSongsResponse.getSongs().size();

        Log.v(LOG_TAG, "onTopSongsEvent - songs size : " + resultsSize);

        if (resultsSize > 0) {
            playerManager.setSongs(topSongsResponse.getSongs());
        } else {
            Toast.makeText(getActivity(), String.format(getResources().getString(R.string.search_results_hint)), Toast.LENGTH_LONG).show();
        }
    }
}
