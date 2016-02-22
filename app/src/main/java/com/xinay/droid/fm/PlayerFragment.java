package com.xinay.droid.fm;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.SharedElementCallback;
import android.graphics.Rect;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.xinay.droid.fm.R;
import com.xinay.droid.fm.async.RadioStationsClient;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.SongArtEvent;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.model.Album;
import com.xinay.droid.fm.model.Image;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.SongArtResponse;
import com.xinay.droid.fm.model.TopSongsResponse;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.services.PlayerService;
import com.xinay.droid.fm.transition.TransitionListenerAdapter;
import com.xinay.droid.fm.util.Constants;
import com.xinay.droid.fm.util.StringUtilities;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.List;
import java.util.Map;


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

    //private MediaPlayer mPlayer;
//    private PlayerService playerService;
//    private PlayerService.PlayerBinder playerBinder;
//
//    private Track mTrack;
//    private String artistName;
//    private TextView mTimeEllapsed;
//    private TextView mTimeDuration;
    private TextView mSongTitle;
    private TextView mArtistName;
    private TextView mStationId;
//    private TextView mAlbumName;
    private ImageView mAlbumCover;
//    private ImageButton mPrevButton;
    private ImageButton mPlayPauseButton;
//    private ImageButton mNextButton;
//    private SeekBar mSeekbar = null;

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
    private Song song;

    private String artist;
    private String albumArtUrl;

    static final String EXTRA_STARTING_ALBUM_POSITION = "extra_starting_item_position";
    static final String EXTRA_CURRENT_ALBUM_POSITION = "extra_current_item_position";

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
    private int mAlbumPosition;
    private int mCurrentPosition;
    private int mStartingPosition;
    private boolean mIsReturning;
    private boolean mIsTransitioning;
    private long mBackgroundImageFadeMillis;

    private Bundle mTmpReenterState;

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

            Log.v(LOG_TAG, "SharedElementCallback - onMapSharedElements() ");

            if (mIsReturning) {
                ImageView sharedElement = mAlbumCover;
                if (sharedElement == null) {
                    // If shared element is null, then it has been scrolled off screen and
                    // no longer visible. In this case we cancel the shared element transition by
                    // removing the shared element from the shared elements map.
                    names.clear();
                    sharedElements.clear();
                } else if (mStartingPosition != mCurrentPosition) {
                    // If the user has swiped to a different ViewPager page, then we need to
                    // remove the old shared element and replace it with the new shared element
                    // that should be transitioned instead.
                    names.clear();
                    names.add(sharedElement.getTransitionName());
                    sharedElements.clear();
                    sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                }
            }
        }
    };


    public static PlayerFragment newInstance(int position, int startingPosition) {
        Bundle args = new Bundle();
        args.putInt(ARG_ALBUM_IMAGE_POSITION, position);
        args.putInt(ARG_STARTING_ALBUM_IMAGE_POSITION, startingPosition);
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setEnterSharedElementCallback(mCallback);

//        mStartingPosition = getArguments().getInt(ARG_STARTING_ALBUM_IMAGE_POSITION);
//        mAlbumPosition = getArguments().getInt(ARG_ALBUM_IMAGE_POSITION);
//        mIsTransitioning = savedInstanceState == null && mStartingPosition == mAlbumPosition;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_layout, container, false);

        mAlbumCover = (ImageView) rootView.findViewById(R.id.album_art);
        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause_button);
        mSongTitle = (TextView) rootView.findViewById(R.id.song_title);
        mArtistName = (TextView) rootView.findViewById(R.id.artist_name);
        mStationId = (TextView) rootView.findViewById(R.id.station_id);

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
            if (song != null) {

//                String key = getParentFragment().toString();
                String key = "6125348712";
                playerManager.getRadioStationsClient().doSongArt(
                        song.getSongArtist(),
                        song.getSongTitle(),
                        Constants.ALBUM_ART_IMAGE_RESOLUTION,
                        key
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
                playPause(song);
                //skip();
            }
        });

        Log.v(LOG_TAG, "song: " + song);
        if (song != null) {
            mSongTitle.setText(song.getSongTitle());
            mArtistName.setText(song.getSongArtist());
            mStationId.setText(song.getCallSign());
        }

        mAlbumImage = (ImageView) rootView.findViewById(R.id.album_art);
        final ImageView backgroundImage = (ImageView) rootView.findViewById(R.id.album_art);

        //View textContainer = rootView.findViewById(R.id.details_text_container);
        TextView albumTitleText = (TextView) rootView.findViewById(R.id.song_title);

        String albumImageUrl = song != null ? song.getAlbumArtUrl() : "http://i.imgur.com/wcMIc6s.jpg";
        String backgroundImageUrl = song != null ? song.getAlbumArtUrl() : "http://i.imgur.com/HvKBJfQ.jpg";
        String albumName = song != null ? song.getSongTitle() : "The King of Limbs";

        albumTitleText.setText(albumName);
        mAlbumImage.setTransitionName(albumName);

        RequestCreator albumImageRequest = Picasso.with(getActivity()).load(albumImageUrl);
        RequestCreator backgroundImageRequest = Picasso.with(getActivity()).load(backgroundImageUrl).fit().centerCrop();

        if (mIsTransitioning) {
            albumImageRequest.noFade();
            backgroundImageRequest.noFade();
            backgroundImage.setAlpha(0f);
            getActivity().getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    backgroundImage.animate().setDuration(mBackgroundImageFadeMillis).alpha(1f);
                }
            });
        }

        albumImageRequest.into(mAlbumImage, mImageCallback);
        backgroundImageRequest.into(backgroundImage);

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


    ///////

    public static PlayerFragment newInstance() {
        PlayerFragment fragment = new PlayerFragment();
//        Bundle args = new Bundle();
//        Parcelable wrapped = Parcels.wrap(track);
//        args.putParcelable(ARG_TRACK, wrapped);
//        args.putString(ARG_ARTIST_NAME, artistName);w
//        fragment.setArguments(args);
        return fragment;
    }

    public PlayerFragment() {
        // Required empty public constructor
        playerManager = PlayerManager.getInstance();
    }

    public void setSong(Song asong) {
        this.song = asong;
        artist = song.getSongArtist();
        Log.v(LOG_TAG, "set song - artist: " + artist);
        Log.v(LOG_TAG, "set song - title: " + song.getSongTitle());
        Log.v(LOG_TAG, "set song - call sign: " + song.getCallSign());
        Log.v(LOG_TAG, "set song - station id: " + song.getStationId());
        Log.v(LOG_TAG, "set song - albumArtUrl: " + albumArtUrl);
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
//        Parcelable wrapped = Parcels.wrap(mTrack);
//        outState.putParcelable(ARG_TRACK, wrapped);
//        outState.putString(ARG_ARTIST_NAME, artistName);
    }


/*
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.v(LOG_TAG, "onCreate(): " + this.toString());
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // Bundle extras = getIntent().getExtras();
            // mTrack = (Track) extras.getSerializable(Constants.TRACK_ID_KEY);
            // artistName = extras.getString(Constants.ARTIST_NAME_KEY);
//            Parcelable wrapped = getArguments().getParcelable(ARG_TRACK);
//            mTrack = Parcels.unwrap(wrapped);
//            artistName = getArguments().getString(ARG_ARTIST_NAME);
        } else {
            //int trackIndex = mTrack.getIndex();
//            int trackIndex = 0;
//            Log.v(LOG_TAG, "onCreate - trackIndex: " + trackIndex);
//            mListener.onPlayerSetTrack(trackIndex);
        }
    }*/

    @Override
    public void onResume() {
//        Log.v(LOG_TAG, "onResume(): " + this.toString());
        super.onResume();
    }

    @Override
    public void onPause() {
//        Log.v(LOG_TAG, "onPause(): " + this.toString());
        super.onPause();
    }

/*    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView(): " + this.toString());
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.player_layout, container, false);

        mAlbumCover = (ImageView) rootView.findViewById(R.id.album_art);
        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause_button);
        mSongTitle = (TextView) rootView.findViewById(R.id.song_title);
        mArtistName = (TextView) rootView.findViewById(R.id.artist_name);
        mStationId = (TextView) rootView.findViewById(R.id.station_id);

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
            if (song != null) {

//                String key = getParentFragment().toString();
                String key = "6125348712";
                playerManager.getRadioStationsClient().doSongArt(
                        song.getSongArtist(),
                        song.getSongTitle(),
                        Constants.ALBUM_ART_IMAGE_RESOLUTION,
                        key
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
                playPause(song);
                //skip();
            }
        });

        Log.v(LOG_TAG, "song: " + song);
        if (song != null) {
            mSongTitle.setText(song.getSongTitle());
            mArtistName.setText(song.getSongArtist());
            mStationId.setText(song.getCallSign());
        }

        return rootView;
    }*/

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        return dialog;
//    }

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

//    private void skip() {
//        if (mListener != null) {
//            song = mListener.onPlayerNext();
//            //updateTrackInfo();
//        }
//    }

//    private void prev() {
//        if (mListener != null) {
//            song = mListener.onPlayerPrev();
//            //updateTrackInfo();
//        }
//    }

    private void updatePosition(){
        handler.removeCallbacks(updatePositionRunnable);
        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }

    private void updateAlbumArt() {

        if (song == null) {
            Log.v(LOG_TAG, "updateAlbumArt , song is null");
            song = playerManager.getCurrentSong();
        }

        Log.v(LOG_TAG, "updateAlbumArt , song - title: " + song.getSongTitle());


//        if (mListener != null) {
//            if (mListener.isPlaying()) {
//                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
//            } else {
//                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
//            }
//        }

//        handler.postDelayed(updatePositionRunnable, UPDATE_DELAY);
    }

    @Subscribe
    public void onSongArtEvent(SongArtEvent event) {
        SongArtResponse songArtResponse = event.response;
        SongArtResponse.SongArt songArt = songArtResponse.getSongArt();
        Log.v(LOG_TAG, "onSongArtEvent - song url : " + songArt.getArtUrl());
        // check for a valid Album Art URL
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

    /*
    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch(v.getId())
            {
                case R.id.play:
                {
                    if(player.isPlaying())
                    {
                        handler.removeCallbacks(updatePositionRunnable);
                        player.pause();
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                    }
                    else
                    {
                        if(isStarted)
                        {
                            player.start();
                            playButton.setImageResource(android.R.drawable.ic_media_pause);

                            updatePosition();
                        }
                        else
                        {
                            startPlay(currentFile);
                        }
                    }

                    break;
                }
                case R.id.next:
                {
                    int seekto = player.getCurrentPosition() + STEP_VALUE;

                    if(seekto > player.getDuration())
                        seekto = player.getDuration();

                    player.pause();
                    player.seekTo(seekto);
                    player.start();

                    break;
                }
                case R.id.prev:
                {
                    int seekto = player.getCurrentPosition() - STEP_VALUE;

                    if(seekto < 0)
                        seekto = 0;

                    player.pause();
                    player.seekTo(seekto);
                    player.start();

                    break;
                }
            }
        }
    };
    */
}
