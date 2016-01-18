package com.xinay.droid.fm.async;

import android.util.Log;

import com.squareup.otto.Produce;
import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.ArtistSearchEvent;
import com.xinay.droid.fm.event.SongArtEvent;
import com.xinay.droid.fm.event.TopSongsEvent;
import com.xinay.droid.fm.event.TopTracksEvent;
import com.xinay.droid.fm.model.ArtistSearchResponse;
import com.xinay.droid.fm.model.PlaylistResponse;
import com.xinay.droid.fm.model.Song;
import com.xinay.droid.fm.model.SongArtResponse;
import com.xinay.droid.fm.model.TopSongsResponse;
import com.xinay.droid.fm.model.TopTracksResponse;
import com.xinay.droid.fm.services.DarStationsResponse;
import com.xinay.droid.fm.services.ServiceHelper;
import com.xinay.droid.fm.util.Constants;
import com.xinay.droid.fm.util.StringUtilities;

import org.parceler.Parcel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

//import retrofit.RestAdapter;
//import retrofit.RetrofitError;
//import retrofit.client.Response;

/**
 * Created by luisvivero on 1/5/16.
 */
public class RadioStationsClient {

    private static final String LOG_TAG = RadioStationsClient.class.getSimpleName();

    public static final String DAR_FM_ENDPOINT = "http://api.dar.fm";

    private static Map<String, String> options = new HashMap<String, String>();

    static {
        options.put("intl", "1");
        options.put("partner_token", "5137886343");
        options.put("callback", "json");
    }

    static class Search {
        String searchTerms;
    }

    interface RadioStationsWebServices {

        @GET("/playlist.php")
        Call<PlaylistResponse> playlist(
                @Query("q") String query
        );

        @GET("/darstations.php")
        Call<DarStationsResponse> stations(
                @Query("q") String query,
                @Query("type") String type
        );

        @GET("/topsongs.php")
        Call<TopSongsResponse> topSongs(@QueryMap Map<String, String> options);

        @GET("/songart.php")
        Call<SongArtResponse> songArt(@QueryMap Map<String, String> options);

    }

    public void doSongArt(String artist, String title, String res) {
        String encodedArtistSearch = "";
        String encodedTitleSearch = "";
        try {
            encodedArtistSearch = StringUtilities.makeUrlEncoded(artist);
            encodedTitleSearch = StringUtilities.makeUrlEncoded(title);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RadioStationsWebServices apiService = ServiceHelper.createService(RadioStationsWebServices.class);

        options.put("artist", encodedArtistSearch);
        options.put("title", encodedTitleSearch);
        options.put("res", res);

        Log.v(LOG_TAG, "doSongArt: " + encodedArtistSearch);

        // asynchronous
        Call<SongArtResponse> call = apiService.songArt(options);
        call.enqueue(new Callback<SongArtResponse>() {
            @Override
            public void onResponse(Response<SongArtResponse> response) {

                Log.v(LOG_TAG, "onResponse code: " + response.code());

                BusProvider.getInstance().post(produceSongArtEvent(response.body()));

                // response.isSuccess() is true if the response code is 2xx
                if (response.isSuccess()) {
                    Log.v(LOG_TAG, "isSuccess - response: " + response.raw());
                    SongArtResponse songArtResponse = response.body();

                    SongArtResponse.SongArt art = songArtResponse.getSongArt();
                    Log.v(LOG_TAG, art.getTitle() + " (" + art.getArtUrl() + ")");

                } else {
                    int statusCode = response.code();

                    // handle request errors yourself
                    ResponseBody errorBody = response.errorBody();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // handle execution failures like no internet connectivity
                Log.v(LOG_TAG, "onFailure message: " + t.getLocalizedMessage());
            }
        });


    }

    public void doTopSongs(String searchTerm){
        String encodedSearch = null;
        try {
            encodedSearch = StringUtilities.makeUrlEncoded(searchTerm);
        } catch (UnsupportedEncodingException e) {
            // TODO toast encoding error
            e.printStackTrace();
            //return null;
        }

        RadioStationsWebServices apiService = ServiceHelper.createService(RadioStationsWebServices.class);

        options.put("q", encodedSearch);


        Log.v(LOG_TAG, "doTopSongs: " + encodedSearch);

        // asynchronous
        Call<TopSongsResponse> call = apiService.topSongs(options);
        call.enqueue(new Callback<TopSongsResponse>() {
            @Override
            public void onResponse(Response<TopSongsResponse> response) {

                Log.v(LOG_TAG, "onResponse code: " + response.code());

                BusProvider.getInstance().post(produceTopSongsEvent(response.body()));

                // response.isSuccess() is true if the response code is 2xx
                if (response.isSuccess()) {
                    Log.v(LOG_TAG, "isSuccess - response: " + response.raw());
                    TopSongsResponse topSongs = response.body();
                    List<Song> songs = topSongs.getSongs();

                    if (songs != null) {
                        for (Song song : songs) {
                            Log.v(LOG_TAG, song.getCallSign() + " (" + song.getSongTitle() + ")");
                        }
                    }
                } else {
                    int statusCode = response.code();

                    // handle request errors yourself
                    ResponseBody errorBody = response.errorBody();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // handle execution failures like no internet connectivity
                Log.v(LOG_TAG, "onFailure message: " + t.getLocalizedMessage());
            }
        });
    }

    @Produce
    public TopSongsEvent produceTopSongsEvent(TopSongsResponse topSongsResponse){
        Log.v(LOG_TAG, "produceTopSongsEvent...");
        return new TopSongsEvent(topSongsResponse);
    }

    @Produce
    public SongArtEvent produceSongArtEvent(SongArtResponse songArtResponse){
        Log.v(LOG_TAG, "produceSongArtEvent...");
        return new SongArtEvent(songArtResponse);
    }

    @Produce
    public TopTracksEvent produceTopTracksEvent(TopTracksResponse tracksResponse){
        return new TopTracksEvent(tracksResponse);
    }

}
