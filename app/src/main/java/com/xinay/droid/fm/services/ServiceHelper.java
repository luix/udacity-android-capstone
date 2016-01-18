package com.xinay.droid.fm.services;

//import com.squareup.okhttp.OkHttpClient;
import com.xinay.droid.fm.util.Constants;

//import retrofit.RestAdapter;
import okhttp3.OkHttpClient;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
//import retrofit.client.OkClient;

/**
 * Created by luisvivero on 7/12/15.
 */
public class ServiceHelper {

    private static SpotifyWebServices SPOTIFY_SERVICES;
    private static RadioStationsWebServices RADIO_STATIONS_SERVICES;

    static {
        setupServiceHelper();
    }

//    public static SpotifyWebServices get() {
//        return SPOTIFY_SERVICES;
//    }

    public static RadioStationsWebServices get() {
        return RADIO_STATIONS_SERVICES;
    }

    private static void setupServiceHelper() {
        /*
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(Constants.API_URL)
                .setClient(new OkClient(new OkHttpClient()));

        builder.setLogLevel(RestAdapter.LogLevel.FULL);

        RestAdapter restAdapter = builder.build();

        SPOTIFY_SERVICES = restAdapter.create(SpotifyWebServices.class);
        */

        Retrofit.Builder builder = new Retrofit.Builder();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API_URL)
                .build();

        RADIO_STATIONS_SERVICES = retrofit.create(RadioStationsWebServices.class);

    }


    public static final String API_BASE_URL = "http://api.dar.fm";

    private static OkHttpClient httpClient = new OkHttpClient();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(serviceClass);
    }
}
