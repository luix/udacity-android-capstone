package com.xinay.droid.fm;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.xinay.droid.fm.R;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Station;
import com.xinay.droid.fm.model.TopSongsResponse;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.services.PlayerService;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements
        SearchFragment.OnFragmentInteractionListener,
        TopTracksFragment.OnFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String PLAYER_MANAGER_KEY = "player_manager";
    private static final String PLAYER_FRAGMENTS_KEY = "player_fragments_list";
    private static final String PLAYER_FRAGMENTS_MAP_KEY = "player_fragments_map";
    private static final String GENRES_FRAGMENTS_MAP_KEY = "genres_fragments_map";

    private Intent playerIntent;

    private PlayerManager playerManager;

    private boolean mMasterDetailPane;

    private List<PlayerFragment> playerFragmentsList;
    private Map<String, GenresFragment> playerFragmentsMap;
    private Map<String, GenresFragment> genresFragmentMap;

    /**
     * The number of pages to show
     */
    private static final int NUM_PAGES = 10;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next album art, song title and artist.
     */
    //private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Parcelable wrapped = Parcels.wrap(tracks);
        //Parcelable wrapped = Parcels.wrap(songs);
        Parcelable wrapped = Parcels.wrap(playerManager);
        Parcelable wrappedGenresMap = Parcels.wrap(genresFragmentMap);
//        Parcelable wrappedFragments = Parcels.wrap(playerFragmentsList);

        //Parcelable wrap = Parcels.wrap(playerService);
        //outState.putParcelable(ARG_TRACKS, wrap);
        //outState.putString(ARG_ARTIST_NAME, artistName);
//        outState.putParcelable(ARG_SONGS, wrapped);
//        outState.putString(ARG_SEARCH_QUERY, searchQuery);
        outState.putParcelable(PLAYER_MANAGER_KEY, wrapped);
        outState.putParcelable(GENRES_FRAGMENTS_MAP_KEY, wrappedGenresMap);
//        outState.putParcelable(PLAYER_FRAGMENTS_KEY, wrappedFragments);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_screen_slide);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //mPager = (ViewPager) findViewById(R.id.viewpager);
        //mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
//        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
//        mPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        showToast("One");
                        break;
                    case 1:
                        showToast("Two");
                        break;
                    case 2:
                        showToast("Three");
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
//            }
//        });

        // For each of the sections in the app, add a tab to the action bar.
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            // Create a tab with text corresponding to the page title defined by
//            // the adapter. Also specify this Activity object, which implements
//            // the TabListener interface, as the callback (listener) for when
//            // this tab is selected.
//            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
//        }


        // register with the bus to receive events
        BusProvider.getInstance().register(this);

        if (savedInstanceState != null) {
            Parcelable wrapped = savedInstanceState.getParcelable(PLAYER_MANAGER_KEY);
            playerManager = Parcels.unwrap(wrapped);
            Parcelable wrappedGenres = savedInstanceState.getParcelable(GENRES_FRAGMENTS_MAP_KEY);
            genresFragmentMap = Parcels.unwrap(wrappedGenres);
//            Parcelable wrappedFragments = savedInstanceState.getParcelable(PLAYER_FRAGMENTS_KEY);
//            playerFragmentsList = Parcels.unwrap(wrappedFragments);
            Log.v(LOG_TAG, "playerManager unwrapped...");
        } else {
            playerManager = PlayerManager.getInstance();
            genresFragmentMap = new HashMap<>();
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

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Do something after 500ms
//                // Instantiate a ViewPager and a PagerAdapter.
//                mPager = (ViewPager) findViewById(R.id.pager);
//                mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
//                mPager.setAdapter(mPagerAdapter);
//            }
//        }, 500);

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

//        if (findViewById(R.id.fragment_container) != null) {
//
//            Log.v(LOG_TAG, "playerFragment...");
//
//            //ArtistListFragment artistListFragment = new ArtistListFragment();
//            PlayerFragment playerFragment = new PlayerFragment();
//
//            // Add the fragment to the 'fragment_container' FrameLayout
//            getFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, playerFragment)
//                    .addToBackStack(null)
//                    .commit();
//        }
    }

    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
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
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }


//    public ActionBar getActionBar() {
//        return this.getActionBar();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // Associate searchable configuration with the SearchView
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.search).getActionView();

        Log.v(LOG_TAG, "search - searchManager: " + searchManager.toString());
        Log.v(LOG_TAG, "search - searchView: " + searchView.toString());

        if (searchView != null) {
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            // Assumes current activity is the searchable activity
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        }

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

//    @Override
//    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
//        // When the given tab is selected, switch to the corresponding page in
//        // the ViewPager.
//        mViewPager.setCurrentItem(tab.getPosition());
//    }
//
//    @Override
//    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
//
//    }
//
//    @Override
//    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
//
//    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            this.addFrag(new GenresFragment(), "R&B");
            this.addFrag(new GenresFragment(), "Rock");
            this.addFrag(new GenresFragment(), "Pop");
            this.addFrag(new GenresFragment(), "Latino");
            this.addFrag(new GenresFragment(), "Top USA");
            this.addFrag(new GenresFragment(), "Christian");
            this.addFrag(new GenresFragment(), "Classical");
            this.addFrag(new GenresFragment(), "Hip Hop");
            this.addFrag(new GenresFragment(), "80's");
            this.addFrag(new GenresFragment(), "90's");
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            // return 3;
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            Locale l = Locale.getDefault();
//            switch (position) {
//                case 0:
//                    return getString(R.string.title_section1).toUpperCase(l);
//                case 1:
//                    return getString(R.string.title_section2).toUpperCase(l);
//                case 2:
//                    return getString(R.string.title_section3).toUpperCase(l);
//            }
            return mFragmentTitleList.get(position);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (playerFragmentsList.size() == position) {
                PlayerFragment playerFragment = new PlayerFragment();
                playerFragment.setSong(playerManager.getSongs().get(position));
                playerFragmentsList.add(position, playerFragment);
                return playerFragment;
            } else {
                return playerFragmentsList.get(position);
            }
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


    /*
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
    */


    @Override
    public void onTrackSelected(Track track) {
        Log.v(LOG_TAG, "onTrackSelected - put extra: track id=" + track.getId());

        FragmentManager fragmentManager = getFragmentManager();
        PlayerFragment newFragment = PlayerFragment.newInstance();

        //trackIndex = track.getIndex();

        if (mMasterDetailPane) {
            // The device is using a large layout, so show the fragment as a dialog
//            newFragment.show(fragmentManager, "dialog");
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

    @Override
    public void onStationSelected(Station station) {
        Log.v(LOG_TAG, "onStationSelected - put extra: station call id=" + station.getStationId());

        FragmentManager fragmentManager = getFragmentManager();
    }

    @Subscribe
    public void onTopSongsEvent(TopSongsEvent event) {
        Log.v(LOG_TAG, "onTopSongsEvent...");

        TopSongsResponse topSongsResponse = event.response;

        int resultsSize = topSongsResponse.getSongs().size();

        String key = topSongsResponse.getKey();

        Log.v(LOG_TAG, "onTopSongsEvent - songs key : " + key);
        Log.v(LOG_TAG, "onTopSongsEvent - songs size : " + resultsSize);

        if (resultsSize > 0) {
            playerManager.setSongs(topSongsResponse.getSongs());

            genresFragmentMap.get(key);

            playerFragmentsList = new ArrayList<PlayerFragment>();

//            mViewPager = (ViewPager) findViewById(R.id.viewpager);
//            mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
//            mViewPager.setAdapter(mPagerAdapter);

        } else {
            Toast.makeText(this, String.format(getResources().getString(R.string.search_results_hint)), Toast.LENGTH_LONG).show();
        }
    }
}
