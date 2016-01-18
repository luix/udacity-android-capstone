package com.xinay.droid.fm.async;

import com.xinay.droid.fm.bus.BusProvider;
import com.xinay.droid.fm.event.ArtistSearchEvent;
import com.xinay.droid.fm.event.TopTracksEvent;
import com.xinay.droid.fm.model.ArtistSearchResponse;
import com.xinay.droid.fm.model.TopTracksResponse;
import com.xinay.droid.fm.util.Constants;
import com.xinay.droid.fm.util.StringUtilities;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

//import retrofit.Callback;
//import retrofit.RestAdapter;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//import retrofit.http.GET;
//import retrofit.http.Path;
//import retrofit.http.Query;
//import retrofit.http.QueryMap;

import com.squareup.otto.Produce;

/**
 * Created by luisvivero on 9/7/15.
 */
public class SpotifyClient {

/*

    public static final String SPOTIFY_ENDPOINT = "https://api.spotify.com/v1";

    private static Map<String, String> options = new HashMap<String, String>();

//    static {
//        options.put("method","flickr.photos.search");
//        options.put("api_key", "8ca2bf66d4ebcce9042bb0cca81f906b");
//        options.put("format", "json");
//        options.put("nojsoncallback", "1");
//    }

    static class Search {
        String searchTerms;
    }

    interface SpotifyWebServices {
        @GET("/search")
        void searchSpotify(@QueryMap Map<String, String> options,
                    Callback<ArtistSearchResponse> callback);

        @GET("/artists/{id}/top-tracks")
        void getTopTracks(@Path("id") String id,
                          @Query("country") String country,
                          Callback<TopTracksResponse> callback);

        //@GET("/")
        //void searchFlickr(@QueryMap Map<String, String> options, Callback<FlickrResult> cb);
    }

    interface ArtistTopTracks {

        @GET("/search")
        void searchSpotify(@QueryMap Map<String, String> options,
                           Callback<ArtistSearchResponse> callback);
        //@GET("/")
        //void searchFlickr(@QueryMap Map<String, String> options, Callback<FlickrResult> cb);
    }

    public void doArtistSearch(String searchTerm){
        String encodedSearch = null;
        try {
            encodedSearch = StringUtilities.makeUrlEncoded(searchTerm);
        } catch (UnsupportedEncodingException e) {
            // TODO toast encoding error
            e.printStackTrace();
            //return null;
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SPOTIFY_ENDPOINT)
                .build();

        SpotifyWebServices apiService = restAdapter.create(SpotifyWebServices.class);

        Callback<ArtistSearchResponse> callback = new Callback<ArtistSearchResponse>(){

            @Override
            public void failure(RetrofitError error) {
                // TODO Auto-generated method stub

            }

            @Override
            public void success(ArtistSearchResponse searchResponse, Response response) {
                BusProvider.getInstance().post(produceArtistSearchEvent(searchResponse));
            }

        };

        options.put("q", encodedSearch);
        options.put("type", Constants.TYPE_ARTISTS);
        apiService.searchSpotify(options, callback);
    }

    public void doTopTracks(String artistId) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SPOTIFY_ENDPOINT)
                .build();

        SpotifyWebServices apiService = restAdapter.create(SpotifyWebServices.class);

        Callback<TopTracksResponse> callback = new Callback<TopTracksResponse>(){

            @Override
            public void failure(RetrofitError error) {
                // TODO Auto-generated method stub

            }

            @Override
            public void success(TopTracksResponse tracksResponse, Response response) {
                BusProvider.getInstance().post(produceTopTracksEvent(tracksResponse));
            }

        };

        apiService.getTopTracks(artistId, Constants.COUNTRY_CODE, callback);
    }


    @Produce
    public ArtistSearchEvent produceArtistSearchEvent(ArtistSearchResponse searchResponse){
        return new ArtistSearchEvent(searchResponse);
    }

    @Produce
    public TopTracksEvent produceTopTracksEvent(TopTracksResponse tracksResponse){
        return new TopTracksEvent(tracksResponse);
    }

*/
}
