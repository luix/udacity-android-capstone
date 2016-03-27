package com.xinay.droid.fm;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.ArraySet;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.otto.Subscribe;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Station;
import com.xinay.droid.fm.model.TopSongsResponse;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.services.PlayerService;
import com.xinay.droid.fm.util.Utilities;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.xinay.droid.fm.util.Constants.EXTRA_GENRE_KEY;
import static com.xinay.droid.fm.util.Constants.EXTRA_CURRENT_ALBUM_POSITION;
import static com.xinay.droid.fm.util.Constants.EXTRA_STARTING_ALBUM_POSITION;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SearchFragment.OnFragmentInteractionListener,
        GenresFragment.OnFragmentInteractionListener {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String PLAYER_MANAGER_KEY = "player_manager";

    private Bundle mTmpReenterState;

    private boolean mIsDetailsActivityStarted;

    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            Log.v(LOG_TAG, "SharedElementCallback...");
            if (mTmpReenterState != null) {
                Log.v(LOG_TAG, "SharedElementCallback , mTmpReenterState != null");
                int startingPosition = mTmpReenterState.getInt(EXTRA_STARTING_ALBUM_POSITION);
                int currentPosition = mTmpReenterState.getInt(EXTRA_CURRENT_ALBUM_POSITION);
                if (startingPosition != currentPosition) {
                    // If startingPosition != currentPosition the user must have swiped to a
                    // different page in the DetailsActivity. We must update the shared element
                    // so that the correct one falls into place.
                    //String newTransitionName = Utilities.makeTransitionKeyName(mCurrentPosition, currentPosition);
                    Log.v(LOG_TAG, "GENRES_MAP_KEYS: " + PlayerManager.GENRES_MAP_KEYS[mCurrentGenresFragmentPosition]);
                    Log.v(LOG_TAG, "currentPosition: " + currentPosition);
                    Song song = PlayerManager.getInstance().getSongsByGenre(PlayerManager.GENRES_MAP_KEYS[mCurrentGenresFragmentPosition]).get(currentPosition);
                    String newTransitionName = song.getUberUrl().getUrl();
                    Log.v(LOG_TAG, "newTransitionName: " + newTransitionName);
                    View newSharedElement = mCurrentGenresFragment.getmGenresRecyclerView().findViewWithTag(newTransitionName);
                    if (newSharedElement != null) {
                        names.clear();
                        names.add(newTransitionName);
                        sharedElements.clear();
                        sharedElements.put(newTransitionName, newSharedElement);
                    }
                }

                mTmpReenterState = null;
            } else {
                // If mTmpReenterState is null, then the activity is exiting.
                View navigationBar = findViewById(android.R.id.navigationBarBackground);
                View statusBar = findViewById(android.R.id.statusBarBackground);
                if (navigationBar != null) {
                    names.add(navigationBar.getTransitionName());
                    sharedElements.put(navigationBar.getTransitionName(), navigationBar);
                }
                if (statusBar != null) {
                    names.add(statusBar.getTransitionName());
                    sharedElements.put(statusBar.getTransitionName(), statusBar);
                }
            }
        }
    };


    private Intent playerIntent;

    private PlayerManager playerManager;
    private boolean mMasterDetailPane;
    private int mCurrentPosition;

    private GoogleApiClient mGoogleApiClient;

    private GenresFragment mCurrentGenresFragment;
    private int mCurrentGenresFragmentPosition;

    private AppBarLayout mAppBarLayout;

    // private List<PlayerFragment> playerFragmentsList;
    // private Map<String, GenresFragment> playerFragmentsMap;

    // private Map<String, GenresFragment> genresFragmentMap = new ArrayMap<>();
//    private Set<String> genresFragmentKeySet = new ArraySet<>();

//    private String topSongsListPlayingKey;

    /**
     * The number of pages to show
     */
//    private static final int NUM_PAGES = 10;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next album art, song title and artist.
     */
    //private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    //private PagerAdapter mPagerAdapter;

//    GenresPagerAdapter mGenresPagerAdapter;

//    ViewPager mViewPager;
//    TabLayout mTabLayout;
    public boolean isDetailsActivityStarted() {
        return mIsDetailsActivityStarted;
    }

    public void setDetailsActivityStarted(boolean detailsActivityStarted) {
        this.mIsDetailsActivityStarted = detailsActivityStarted;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Parcelable wrapped = Parcels.wrap(tracks);
        //Parcelable wrapped = Parcels.wrap(songs);
        Parcelable wrapped = Parcels.wrap(playerManager);
//        Parcelable wrappedGenresMap = Parcels.wrap(genresFragmentMap);
//        Parcelable wrappedFragments = Parcels.wrap(playerFragmentsList);

        //Parcelable wrap = Parcels.wrap(playerService);
        //outState.putParcelable(ARG_TRACKS, wrap);
        //outState.putString(ARG_ARTIST_NAME, artistName);
//        outState.putParcelable(ARG_SONGS, wrapped);
//        outState.putString(ARG_SEARCH_QUERY, searchQuery);
        outState.putParcelable(PLAYER_MANAGER_KEY, wrapped);
//        outState.putParcelable(GENRES_FRAGMENTS_MAP_KEY, wrappedGenresMap);
//        outState.putParcelable(PLAYER_FRAGMENTS_KEY, wrappedFragments);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAppBarLayout.setExpanded(false);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            Log.v(LOG_TAG, "new GoogleApiClient.Builder()...");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.v(LOG_TAG, "mGoogleApiClient build!");
        }

        if (savedInstanceState != null) {
            Parcelable wrapped = savedInstanceState.getParcelable(PLAYER_MANAGER_KEY);
            playerManager = Parcels.unwrap(wrapped);
//            Parcelable wrappedGenres = savedInstanceState.getParcelable(GENRES_FRAGMENTS_MAP_KEY);
//            genresFragmentMap = Parcels.unwrap(wrappedGenres);
//            Parcelable wrappedFragments = savedInstanceState.getParcelable(PLAYER_FRAGMENTS_KEY);
//            playerFragmentsList = Parcels.unwrap(wrappedFragments);
            Log.v(LOG_TAG, "playerManager unwrapped...");
        } else {

            playerManager = PlayerManager.getInstance();

//            if (playerFragmentsList == null) {
//                Log.v(LOG_TAG, "playerFragmentsList = new ArrayList ");
//                playerFragmentsList = new ArrayList<PlayerFragment>();
//            }

        }


        //initGenres();
        Log.v(LOG_TAG, "playerManager.init()...");
        playerManager.init();

        Log.v(LOG_TAG, "playerManager.startup()...");
        playerManager.startup();

        // Set up the action bar.
//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //mGenresPagerAdapter = new GenresPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        //mViewPager = (ViewPager) findViewById(R.id.viewpager);
        //mViewPager.setAdapter(mGenresPagerAdapter);
        final ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(new GenresFragmentPagerAdapter(getFragmentManager()));
        pager.setCurrentItem(mCurrentPosition);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }
        });


        //mPager = (ViewPager) findViewById(R.id.viewpager);
        //mPagerAdapter = new PlayNowSlidePagerAdapter(getFragmentManager());
//        mGenresPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
//        mPager.setAdapter(mGenresPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.v(LOG_TAG, "onTabSelected , tab: " + tab.getTag());
                pager.setCurrentItem(tab.getPosition());
//                switch (tab.getPosition()) {
//                    case 0:
//                        showToast("One");
//                        break;
//                    case 1:
//                        showToast("Two");
//                        break;
//                    case 2:
//                        showToast("Three");
//                        break;
//                }
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
//        for (int i = 0; i < mGenresPagerAdapter.getCount(); i++) {
//            // Create a tab with text corresponding to the page title defined by
//            // the adapter. Also specify this Activity object, which implements
//            // the TabListener interface, as the callback (listener) for when
//            // this tab is selected.
//            actionBar.addTab(actionBar.newTab().setText(mGenresPagerAdapter.getPageTitle(i)).setTabListener(this));
//        }

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
//                mPagerAdapter = new PlayNowSlidePagerAdapter(getFragmentManager());
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

        setExitSharedElementCallback(mCallback);


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

    @Override
    protected void onResume() {
        super.onResume();
        mIsDetailsActivityStarted = false;
    }

    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        Log.v(LOG_TAG, "onActivityReenter");
        super.onActivityReenter(requestCode, data);
        mTmpReenterState = new Bundle(data.getExtras());
        int startingPosition = mTmpReenterState.getInt(EXTRA_STARTING_ALBUM_POSITION);
        int currentPosition = mTmpReenterState.getInt(EXTRA_CURRENT_ALBUM_POSITION);
        if (startingPosition != currentPosition) {
//            mRecyclerView.scrollToPosition(currentPosition);
            Log.v(LOG_TAG, "mGenresRecyclerView.scrollToPosition()...");
            mCurrentGenresFragment.getmGenresRecyclerView().scrollToPosition(currentPosition);
        }
        postponeEnterTransition();

        mCurrentGenresFragment.getmGenresRecyclerView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.v(LOG_TAG, "getViewTreeObserver().addOnPreDrawListener()...");
                mCurrentGenresFragment.getmGenresRecyclerView().getViewTreeObserver().removeOnPreDrawListener(this);
                // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                mCurrentGenresFragment.getmGenresRecyclerView().requestLayout();
                startPostponedEnterTransition();
                return true;
            }
        });
    }


    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        Log.v(LOG_TAG, "onStart");
        if (mGoogleApiClient != null) {
            Log.v(LOG_TAG, "mGoogleApiClient.connect()");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }


    @Override
    protected void onStop() {
        Log.v(LOG_TAG, "onStop");
        if (mGoogleApiClient != null) {
            Log.v(LOG_TAG, "mGoogleApiClient.disconnect()");
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Log.v(LOG_TAG, "onBackPressed");

//        if (mViewPager.getAdapter() == mGenresPagerAdapter) {
//
//            if (mViewPager.getCurrentItem() == 0) {
//                // If the user is currently looking at the first step, allow the system to handle the
//                // Back button. This calls finish() on this activity and pops the back stack.
//                int count = getFragmentManager().getBackStackEntryCount();
//                if (count == 0) {
//                    super.onBackPressed();
//                } else {
//                    getFragmentManager().popBackStack();
//                }
//
//            } else {
//                // Otherwise, select the previous step.
//                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
//            }
//        } else {
//            mViewPager.setAdapter(mGenresPagerAdapter);
//            mTabLayout.setupWithViewPager(mViewPager);
//            mTabLayout.setVisibility(View.VISIBLE);
//            mPagerAdapter = null;
//        }
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
//            Log.v(LOG_TAG, "searchView.setSearchableInfo()...");
//            searchView.setSearchableInfo(
//                    searchManager.getSearchableInfo(getComponentName()));
            // Assumes current activity is the searchable activity
            Log.v(LOG_TAG, "searchView.setSearchableInfo()...");
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(LOG_TAG, "onConnected()...");
        // Check if Corase Location Permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (location != null) {
            Tracker tracker = ((DroidfmApplication)getApplication()).getDefaultTracker();
            tracker.setLocation("Location~Latitude" + String.valueOf(location.getLatitude()) + "~Longitude~" + String.valueOf(location.getLongitude()));
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(LOG_TAG, "onConnectionSuspended()...");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(LOG_TAG, "onConnectionFailed()...");
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
/*    public class GenresPagerAdapter extends FragmentStatePagerAdapter {
//        private final List<Fragment> mFragmentList = new ArrayList<>();
//        private final List<String> mFragmentTitleList = new ArrayList<>();

        private final String[] titles = {
                "Top USA",
                "Rock",
                "80's",
                "90's",
                "Hip Hop",
                "Clasica"
        };

        public GenresPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (genresFragmentMap.size() == position) {
                final GenresFragment fragment = new GenresFragment();
                fragment.setKey(PlayerManager.GENRES_MAP_KEYS[position]);
                this.addFrag(PlayerManager.GENRES_MAP_KEYS[position], fragment);
                return fragment;
            } else {
                return genresFragmentMap.get(PlayerManager.GENRES_MAP_KEYS[position]);
            }
        }

        public void addFrag(String key, GenresFragment fragment) {
            genresFragmentMap.put(key, fragment);
        }

        @Override
        public int getCount() {
            return titles.length;
            //return genresFragmentMap.size();
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
            return titles[position];
        }
    }*/

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
/*
    private class PlayNowSlidePagerAdapter extends FragmentStatePagerAdapter {
        public PlayNowSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (playerFragmentsList.size() == position) {
                PlayerFragment playerFragment = new PlayerFragment();
                playerFragment.setSong(playerManager.getSongsByGenre(topSongsListPlayingKey).get(position));
                playerFragmentsList.add(position, playerFragment);
                return playerFragment;
            } else {
                return playerFragmentsList.get(position);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(position);
        }

        @Override
        public int getCount() {
            return playerFragmentsList.size();
        }
    }
*/


//    @Override
//    public void onActivityReenter(int requestCode, Intent data) {
//        super.onActivityReenter(requestCode, data);
////        postponeEnterTransition();
//    }

    @Override
    public void onSongSelected(Song song, ImageView imageView) {
        Log.v(LOG_TAG, "onSongSelected - song title: " + song.getSongTitle());
        Log.v(LOG_TAG, "onSongSelected - group key: " + song.getGroupKey());

//        topSongsListPlayingKey = song.getGroupKey();

        playerManager.setCurrentSong(song);
        List<Song> songs = playerManager.getSongsByGenre(song.getGroupKey());
        playerManager.setSongs(songs);

//        int songIndex = 0;
//        playerFragmentsList = new ArrayList<PlayerFragment>();
//        for (int i = 0; i < songs.size(); i++) {
//            PlayerFragment playerFragment = PlayerFragment.newInstance(3, 3);
//            Song songInList = playerManager.getSongsByGenre(topSongsListPlayingKey).get(i);
//            if (songInList.getSongTitle().equals(song.getSongTitle())) songIndex = i;
//            playerFragment.setSong(songInList);
//            playerFragmentsList.add(i, playerFragment);
//        }
//        playerFragmentsList.notify();

        //mViewPager = (ViewPager) findViewById(R.id.viewpager);
//        mPagerAdapter = new PlayNowSlidePagerAdapter(getFragmentManager());
//        mViewPager.clearOnPageChangeListeners();
//        mViewPager.setAdapter(mPagerAdapter);
//        mTabLayout.setupWithViewPager(mViewPager);
//        mTabLayout.setVisibility(View.INVISIBLE);
//        mPagerAdapter.notifyDataSetChanged();
////        mViewPager.notify();
//
//        Log.v(LOG_TAG, "songIndex: " + songIndex);
//        Log.v(LOG_TAG, "mViewPager.getChildCount(): " + mViewPager.getChildCount());
//        mViewPager.setCurrentItem(songIndex);


//        if (findViewById(R.id.fragment_container) != null) {
//            Log.v(LOG_TAG, "playerFragment...");
        //ArtistListFragment artistListFragment = new ArtistListFragment();
//            PlayerFragment playerFragment = new PlayerFragment();
//            playerFragment.setSong(song);
//
//            playerFragmentsList.add(playerFragment);


        // Add the fragment to the 'fragment_container' FrameLayout
//            getFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, playerFragmentsList.get(songIndex))
//                    .addToBackStack(null)
//                    .commit();
//        }
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


    //@Override
    public void onTrackSelected(Track track) {
        Log.v(LOG_TAG, "onTrackSelected - put extra: track id=" + track.getId());

        FragmentManager fragmentManager = getFragmentManager();
//        PlayerFragment newFragment = PlayerFragment.newInstance(4, 4);

        //trackIndex = track.getIndex();

        if (mMasterDetailPane) {
            // The device is using a large layout, so show the fragment as a dialog
//            newFragment.show(fragmentManager, "dialog");
        } else {
            // The device is smaller, so show the fragment fullscreen
//            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
//            transaction.add(android.R.id.content, newFragment)
//                    .addToBackStack(null).commit();
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

    private class GenresFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private final String[] titles = {
                "Top USA",
                "Rock",
                "80's",
                "90's",
                "Hip Hop",
                "Clasica"
        };

        public GenresFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            Log.v(LOG_TAG, "GenresFragmentPagerAdapter(fm)");
        }

        @Override
        public Fragment getItem(int position) {
            Log.v(LOG_TAG, "GenresFragmentPagerAdapter , getItem: " + position + " , key: " + PlayerManager.GENRES_MAP_KEYS[position]);

            Tracker tracker = ((DroidfmApplication)getApplication()).getDefaultTracker();
            tracker.setScreenName("GenreScreen~" + PlayerManager.GENRES_MAP_KEYS[position]);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());

            return GenresFragment.newInstance(PlayerManager.GENRES_MAP_KEYS[position]);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentGenresFragmentPosition = position;
            mCurrentGenresFragment = (GenresFragment) object;
        }

        @Override
        public int getCount() {
//            Log.v(LOG_TAG, "GenresFragmentPagerAdapter , getCount: " + titles.length);
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
