package com.xinay.droid.fm.services;

import com.xinay.droid.fm.model.PlaylistResponse;
import com.xinay.droid.fm.model.SongArtResponse;
import com.xinay.droid.fm.model.TopSongsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by luisvivero on 1/3/16.
 */
public interface RadioStationsWebServices {

    @GET("/playlist.php")
    Call<PlaylistResponse> playlist(@Query("q") String query,
                                    @Query("partner_token") String partnerToken);

    @GET("/darstations.php")
    Call<DarStationsResponse> stations(@Query("q") String query,
                                       @Query("type") String type,
                                       @Query("partner_token") String partnerToken);

    @GET("/topsongs.php")
    Call<TopSongsResponse> topSongs(@Query("q") String query,
                                    @Query("type") String type,
                                    @Query("partner_token") String partnerToken);

    @GET("/songart.php")
    Call<SongArtResponse> songArt(@Query("artist") String artist,
                                  @Query("title") String title,
                                  @Query("res") String res,
                                  @Query("partner_token") String partnerToken);

//    @GET("/repos/{owner}/{repo}/contributors")
//    Call<List<Contributor>> contributors(
//            @Path("owner") String owner,
//            @Path("repo") String repo
//    );
}
