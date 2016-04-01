package com.xinay.droid.fm;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.xinay.droid.fm.R;
import com.xinay.droid.fm.model.Song;

import java.util.List;
import java.util.Map;

import static com.xinay.droid.fm.util.Constants.EXTRA_GENRE_KEY;
import static com.xinay.droid.fm.util.Constants.EXTRA_CURRENT_ALBUM_POSITION;
import static com.xinay.droid.fm.util.Constants.EXTRA_STARTING_ALBUM_POSITION;

public class PlayerActivity extends AppCompatActivity implements
        PlayerFragment.OnFragmentInteractionListener {

    private final String LOG_TAG = PlayerActivity.class.getSimpleName();

    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            Log.v(LOG_TAG, "SharedElementCallback - onMapSharedElements(..)");
            if (mIsReturning) {
                ImageView sharedElement = mCurrentDetailsFragment.getAlbumImage();
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
                    Log.v(LOG_TAG, "sharedElement.getTransitionName: " + sharedElement.getTransitionName());
                    names.add(sharedElement.getTransitionName());
                    sharedElements.clear();
                    sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                }
            }
        }
    };

    private PlayerFragment mCurrentDetailsFragment;
    private String mGenre;
    private int mCurrentPosition;
    private int mStartingPosition;
    private boolean mIsReturning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Log.v(LOG_TAG, "postponeEnterTransition()");
        postponeEnterTransition();
        Log.v(LOG_TAG, "setEnterSharedElementCallback()");
        setEnterSharedElementCallback(mCallback);

        mGenre = getIntent().getStringExtra(EXTRA_GENRE_KEY);
        mStartingPosition = getIntent().getIntExtra(EXTRA_STARTING_ALBUM_POSITION, 0);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }
        Log.v(LOG_TAG, "mStartingPosition: " + mStartingPosition);
        Log.v(LOG_TAG, "mCurrentPosition: " + mCurrentPosition);

        Log.v(LOG_TAG, "ViewPager...");
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new DetailsFragmentPagerAdapter(getFragmentManager()));
        pager.setCurrentItem(mCurrentPosition);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                Log.v(LOG_TAG, "addOnPageChangeListener , mCurrentPosition: " + mCurrentPosition);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, mCurrentPosition);
    }

    @Override
    public void finishAfterTransition() {
        Log.v(LOG_TAG, "finishAfterTransition()");
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(EXTRA_STARTING_ALBUM_POSITION, mStartingPosition);
        data.putExtra(EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    private class DetailsFragmentPagerAdapter extends FragmentStatePagerAdapter {
        public DetailsFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Song song = PlayerManager.getInstance().getSongsByGenre(mGenre).get(position);

            Tracker tracker = ((DroidfmApplication)getApplication()).getDefaultTracker();
            tracker.setScreenName("StationScreen~" + song.getStationId() + "~Artist~" + song.getSongArtist() + "~SongTitle~" + song.getSongTitle());
            tracker.send(new HitBuilders.ScreenViewBuilder().build());

            return PlayerFragment.newInstance(mGenre, position, mStartingPosition);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentDetailsFragment = (PlayerFragment) object;
        }

        @Override
        public int getCount() {
            return PlayerManager.getInstance().getSongsByGenre(mGenre).size();
        }
    }


    @Override
    public void onPlayerPlayPause() {
        Log.v(LOG_TAG, "onPlayerPlayPause");
        PlayerManager.getInstance().onPlayerPlayPause();
    }

    @Override
    public void onPreparePlayer(Song song) {
        Log.v(LOG_TAG, "onPreparePlayer");
        PlayerManager.getInstance().onPrepareSong(song);
    }

    @Override
    public boolean isPlaying() {
        return PlayerManager.getInstance().isPlaying();
    }

    /////////////

/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (findViewById(R.id.fragment_container) != null) {

            Log.v(LOG_TAG, "playerFragment...");

            //ArtistListFragment artistListFragment = new ArtistListFragment();
            PlayerFragment playerFragment = PlayerFragment.newInstance();
//            playerFragment.setSong(playerManager.getSongsByGenre(topSongsListPlayingKey).get(position));

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, playerFragment)
                    .setTransition()
                    .setTransitionStyle()
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
