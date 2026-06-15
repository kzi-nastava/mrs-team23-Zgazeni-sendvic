package com.example.mobile_front_ma.data.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit clients for the public OpenStreetMap services used by ride estimation:
 * Nominatim (geocoding) and OSRM (routing). These live on different hosts than the app's
 * own Spring backend ({@link ApiClient}), so they get their own Retrofit instances here.
 */
public final class GeoApiClient {

    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final String OSRM_BASE_URL = "https://router.project-osrm.org/";

    // Nominatim's usage policy requires an identifying User-Agent on every request.
    private static final String USER_AGENT = "ZgazeniSendvicMA/1.0 (SIIT student project)";

    private static OkHttpClient httpClient;
    private static NominatimApi nominatimApi;
    private static OsrmApi osrmApi;

    private GeoApiClient() {
    }

    public static NominatimApi nominatim() {
        if (nominatimApi == null) {
            nominatimApi = new Retrofit.Builder()
                    .baseUrl(NOMINATIM_BASE_URL)
                    .client(client())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(NominatimApi.class);
        }
        return nominatimApi;
    }

    public static OsrmApi osrm() {
        if (osrmApi == null) {
            osrmApi = new Retrofit.Builder()
                    .baseUrl(OSRM_BASE_URL)
                    .client(client())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(OsrmApi.class);
        }
        return osrmApi;
    }

    private static OkHttpClient client() {
        if (httpClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            httpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                    .header("User-Agent", USER_AGENT)
                                    .build()))
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return httpClient;
    }
}
