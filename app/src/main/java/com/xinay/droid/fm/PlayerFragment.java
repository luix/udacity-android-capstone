package com.xinay.droid.fm;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.xinay.droid.R;
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
import com.xinay.droid.fm.util.Constants;
import com.xinay.droid.fm.util.StringUtilities;

import org.parceler.Parcels;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends DialogFragment {

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
        song = playerManager.getCurrentSong();
    }

    public interface OnFragmentInteractionListener {
        public void onPlayerPlayPause();
        public Song onPlayerNext();
        public Song onPlayerPrev();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // register with the bus to receive events
        BusProvider.getInstance().register(this);

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.player_layout, container, false);

        // ((TextView) rootView.findViewById(R.id.artist_name)).setText(song.getSongTitle());

//        mSong = (TextView) rootView.findViewById(R.id.track_name);
        mSongTitle = (TextView) rootView.findViewById(R.id.song_title);
//        mTimeEllapsed = (TextView) rootView.findViewById(R.id.time_ellapsed);
//        mTimeDuration = (TextView) rootView.findViewById(R.id.time_total);
//        mPrevButton = (ImageButton) rootView.findViewById(R.id.prev_button);
        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.play_pause_button);
//        mNextButton = (ImageButton) rootView.findViewById(R.id.next_button);
//        mSeekbar = (SeekBar) rootView.findViewById(R.id.seek_bar);
//        mAlbumName = (TextView) rootView.findViewById(R.id.album_name);
        mArtistName = (TextView) rootView.findViewById(R.id.artist_name);
        mStationId = (TextView) rootView.findViewById(R.id.station_id);
        mAlbumCover = (ImageView) rootView.findViewById(R.id.album_art);

        //updateTrackInfo();

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });
//        mPrevButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                prev();
//            }
//        });
//        mNextButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                skip();
//            }
//        });
//
//        mSeekbar.setOnSeekBarChangeListener(seekBarChanged);
//        mSeekbar.setProgress(0);
//        mSeekbar.setMax(PREVIEW_SONG_DURATION);

        return rootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void playPause() {
        Log.v(LOG_TAG, "playPause()");
        if (mListener != null) {
            mListener.onPlayerPlayPause();
            if (mListener.isPlaying()) {
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        }

        Log.v(LOG_TAG, "updateAlbumArt()");
        updateAlbumArt();
    }

    private void skip() {
        if (mListener != null) {
            song = mListener.onPlayerNext();
            //updateTrackInfo();
        }
    }

    private void prev() {
        if (mListener != null) {
            song = mListener.onPlayerPrev();
            //updateTrackInfo();
        }
    }

    private void updatePosition(){
        handler.removeCallbacks(updatePositionRunnable);
        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }

    private void updateAlbumArt() {

        if (song == null) {
            song = playerManager.getCurrentSong();
            Log.v(LOG_TAG, "song - title: " + song.getSongTitle());
        }

        playerManager.getRadioStationsClient().doSongArt(
                song.getSongArtist(),
                song.getSongTitle(),
                Constants.ALBUM_ART_IMAGE_RESOLUTION
        );

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

        Log.v(LOG_TAG, "onSongArtEvent - songs size : " + songArt.getArtUrl());

        // check for a valid Album Art URL
        if (Patterns.WEB_URL.matcher(songArt.getArtUrl()).matches()) {
            Picasso.with(getActivity())
                    .load(songArt.getArtUrl())
                    .into(mAlbumCover);
        }


    }

//    private void updateTrackInfo() {
//        mTimeEllapsed.setText("0:00");
//        mTimeDuration.setText("0:30");
//    }

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
