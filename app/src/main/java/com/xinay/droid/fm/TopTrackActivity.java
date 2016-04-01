package com.xinay.droid.fm;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.xinay.droid.fm.R;
import com.xinay.droid.fm.model.Track;
import com.xinay.droid.fm.util.Constants;

/**
 * Created by luisvivero on 7/13/15.
 */
public class TopTrackActivity extends AppCompatActivity {

    private final String LOG_TAG = TopTrackActivity.class.getSimpleName();

    private String artistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            String artistId = getIntent().getStringExtra(Constants.ARTIST_ID_KEY);
            artistName = getIntent().getStringExtra(Constants.ARTIST_NAME_KEY);

            Bundle arguments = new Bundle();
            arguments.putString(Constants.ARTIST_ID_KEY, artistId);
            arguments.putString(Constants.ARTIST_NAME_KEY, artistName);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(arguments);

            getFragmentManager().beginTransaction()
                    .add(R.id.top_tracks_container, fragment)
                    .commit();
        }
    }

    public void onArtistSelected(Track track) {
        Log.v(LOG_TAG, "onItemSelected - put extra: track id=" + track.getId());

        FragmentManager fragmentManager = getFragmentManager();
        PlayerFragment newFragment = new PlayerFragment();

//        if (mIsLargeLayout) {
//            // The device is using a large layout, so show the fragment as a dialog
//            newFragment.show(fragmentManager, "dialog");
//        } else {
//            // The device is smaller, so show the fragment fullscreen
//            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            // For a little polish, specify a transition animation
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            // To make it fullscreen, use the 'content' root view as the container
//            // for the fragment, which is always the root view for the activity
//            transaction.add(android.R.id.content, newFragment)
//                    .addToBackStack(null).commit();
//        }

//        Intent intent = new Intent(this, PlayerActivity.class)
//                .putExtra(Constants.ARTIST_NAME_KEY, artistName)
//                .putExtra(Constants.TRACK_ID_KEY, (Serializable) track);
//        startActivity(intent);
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
        if (id == R.id.action_settings) {
            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
