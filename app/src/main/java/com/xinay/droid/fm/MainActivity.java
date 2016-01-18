package com.xinay.droid.fm;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

//import android.widget.SearchView;

import com.squareup.otto.Subscribe;
import com.xinay.droid.R;
import com.xinay.droid.fm.async.RadioStationsClient;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.model.Artist;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.TopSongsResponse;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.services.PlayerService;
import com.xinay.droid.fm.services.PlayerService.PlayerBinder;
import com.xinay.droid.fm.util.Constants;

import org.parceler.Parcels;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ArtistListFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String PLAYER_MANAEGER_KEY = "player_manager";

    private Intent playerIntent;

    private PlayerManager playerManager;

    private boolean mMasterDetailPane;

    /**
     * The number of pages to show
     */
    private static final int NUM_PAGES = 10;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next album art, song title and artist.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Parcelable wrapped = Parcels.wrap(tracks);
        //Parcelable wrapped = Parcels.wrap(songs);
        Parcelable wrapped = Parcels.wrap(playerManager);

        //Parcelable wrap = Parcels.wrap(playerService);
        //outState.putParcelable(ARG_TRACKS, wrap);
        //outState.putString(ARG_ARTIST_NAME, artistName);
//        outState.putParcelable(ARG_SONGS, wrapped);
//        outState.putString(ARG_SEARCH_QUERY, searchQuery);
        outState.putParcelable(PLAYER_MANAEGER_KEY, wrapped);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        // register with the bus to receive events
        BusProvider.getInstance().register(this);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        if (savedInstanceState != null) {
            Parcelable wrapped = savedInstanceState.getParcelable(PLAYER_MANAEGER_KEY);
            playerManager = Parcels.unwrap(wrapped);
            Log.v(LOG_TAG, "playerManager unwrapped...");
        } else {
            playerManager = PlayerManager.getInstance();
            Log.v(LOG_TAG, "playerManager.init()...");
            playerManager.init();
        }

        Log.v(LOG_TAG, "playerManager.startup()...");
        playerManager.startup();

        if (playerIntent == null || playerManager.getPlayerService() == null) {
            Log.v(LOG_TAG, "playerIntent...");
            playerIntent = new Intent(this, PlayerService.class);
            Log.v(LOG_TAG, "bindService...");
            bindService(playerIntent, playerManager.getMusicConnection(), Context.BIND_AUTO_CREATE);
            Log.v(LOG_TAG, "startService...");
            startService(playerIntent);
        }

        /*if (findViewById(R.id.top_tracks_container) != null) {
            mMasterDetailPane = true;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.top_tracks_container, new TopTracksFragment())
                        .commit();
            }
        } else {
            mMasterDetailPane = false;
        }*/

        Log.v(LOG_TAG, "playerService...");
        Intent playerService = new Intent(this, PlayerService.class);
        startService(playerService);

        if (findViewById(R.id.fragment_container) != null) {

            Log.v(LOG_TAG, "playerFragment...");

            //ArtistListFragment artistListFragment = new ArtistListFragment();
            PlayerFragment playerFragment = new PlayerFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, playerFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            int count = getFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                super.onBackPressed();
            } else {
                getFragmentManager().popBackStack();
            }

        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


    public ActionBar getActionBar() {
        return this.getActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new PlayerFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onPlayerPlayPause() {
        Log.v(LOG_TAG, "onPlayerPlayPause");
        playerManager.onPlayerPlayPause();
    }

    @Override
    public Song onPlayerNext() {
        Log.v(LOG_TAG, "onPlayerNext");
        return playerManager.onPlayerNext();
    }

    @Override
    public Song onPlayerPrev() {
        Log.v(LOG_TAG, "onPlayerPrev");
        return playerManager.onPlayerPrev();
    }

    @Override
    public boolean isPlaying() {
        return playerManager.isPlaying();
    }


    @Override
    public void onArtistSelected(Artist artist) {
        Log.v(LOG_TAG, "onArtistSelected - put extra: artist id=" + artist.getId());

        Bundle args = new Bundle();
        args.putString(Constants.ARTIST_ID_KEY, artist.getId());
        args.putString(Constants.ARTIST_NAME_KEY, artist.getName());

        TopTracksFragment fragment = TopTracksFragment.newInstance(artist.getId(), artist.getName());
        fragment.setArguments(args);

        if (mMasterDetailPane) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, fragment)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
//            Intent intent = new Intent(this, TopTrackActivity.class)
//                    .putExtra(Constants.ARTIST_ID_KEY, artist.getId())
//                    .putExtra(Constants.ARTIST_NAME_KEY, artist.getName());
//            startActivity(intent);
        }

    }

    @Override
    public void onTrackSelected(Track track) {
        Log.v(LOG_TAG, "onTrackSelected - put extra: track id=" + track.getId());

        FragmentManager fragmentManager = getFragmentManager();
        PlayerFragment newFragment = PlayerFragment.newInstance();

        //trackIndex = track.getIndex();

        if (mMasterDetailPane) {
            // The device is using a large layout, so show the fragment as a dialog
            newFragment.show(fragmentManager, "dialog");
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, newFragment)
                    .addToBackStack(null).commit();
        }
//        Intent intent = new Intent(this, PlayerActivity.class)
//                .putExtra(Constants.ARTIST_NAME_KEY, artistName)
//                .putExtra(Constants.TRACK_ID_KEY, (Serializable) track);
//        startActivity(intent);
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
            Toast.makeText(this, String.format(getResources().getString(R.string.search_results_hint)), Toast.LENGTH_LONG).show();
        }
    }
}
