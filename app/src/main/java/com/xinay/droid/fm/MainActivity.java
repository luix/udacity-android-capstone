package com.xinay.droid.fm;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.SimpleCursorAdapter;
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
import com.xinay.droid.fm.database.StationsTable;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.Station;
import com.xinay.droid.fm.model.TopSongsResponse;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.provider.RadioStationsContentProvider;
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
        GenresFragment.OnFragmentInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String PLAYER_MANAGER_KEY = "player_manager";

    private Bundle mTmpReenterState;

    private boolean mIsDetailsActivityStarted;

    // private Cursor cursor;
    private SimpleCursorAdapter adapter;

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
        Parcelable wrapped = Parcels.wrap(playerManager);
        outState.putParcelable(PLAYER_MANAGER_KEY, wrapped);
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
            Log.v(LOG_TAG, "playerManager unwrapped...");
        } else {

            playerManager = PlayerManager.getInstance();
        }

        //initGenres();

        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[] { StationsTable.COLUMN_SUMMARY };
        // Fields on the UI to which we map
        int[] to = new int[] { R.id.station_call_sign };

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(this, R.layout.activity_screen_slide, null, from,
                to, 0);

        Log.v(LOG_TAG, "playerManager.init()...");
        playerManager.init();

        Log.v(LOG_TAG, "playerManager.startup()...");
        playerManager.startup();

        // Set up the ViewPager with the sections adapter.
        final ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(new GenresFragmentPagerAdapter(getFragmentManager()));
        pager.setCurrentItem(mCurrentPosition);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }
        });


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.v(LOG_TAG, "onTabSelected , tab: " + tab.getTag());
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        if (playerIntent == null || playerManager.getPlayerService() == null) {
            Log.v(LOG_TAG, "playerIntent...");
            playerIntent = new Intent(this, PlayerService.class);
            Log.v(LOG_TAG, "bindService...");
            bindService(playerIntent, playerManager.getMusicConnection(), Context.BIND_AUTO_CREATE);
            Log.v(LOG_TAG, "startService...");
            startService(playerIntent);
        }


        Log.v(LOG_TAG, "playerService...");
        Intent playerService = new Intent(this, PlayerService.class);
        startService(playerService);

        setExitSharedElementCallback(mCallback);
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
            //mRecyclerView.scrollToPosition(currentPosition);
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Associate searchable configuration with the SearchView
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        Log.v(LOG_TAG, "search - searchManager: " + searchManager.toString());
        Log.v(LOG_TAG, "search - searchView: " + searchView.toString());

        if (searchView != null) {
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

    @Override
    public void onSongSelected(Song song, ImageView imageView) {
        Log.v(LOG_TAG, "onSongSelected - song title: " + song.getSongTitle());
        Log.v(LOG_TAG, "onSongSelected - group key: " + song.getGroupKey());

        playerManager.setCurrentSong(song);
        List<Song> songs = playerManager.getSongsByGenre(song.getGroupKey());
        playerManager.setSongs(songs);
    }

    //@Override
    public void onTrackSelected(Track track) {
        Log.v(LOG_TAG, "onTrackSelected - put extra: track id=" + track.getId());

        FragmentManager fragmentManager = getFragmentManager();
        //trackIndex = track.getIndex();
    }

    @Override
    public void onStationSelected(Station station) {
        Log.v(LOG_TAG, "onStationSelected - put extra: station call id=" + station.getStationId());

        FragmentManager fragmentManager = getFragmentManager();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { StationsTable.COLUMN_ID, StationsTable.COLUMN_SUMMARY };
        CursorLoader cursorLoader = new CursorLoader(this,
                RadioStationsContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
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
