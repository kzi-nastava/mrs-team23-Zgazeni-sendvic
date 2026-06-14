package com.example.mobile_front_ma.data.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Single shared Retrofit instance for the whole app.
 *
 * Retrofit + Gson is used so we declare the REST endpoints once (see {@link AuthApi})
 * and let the library handle JSON (de)serialization and background threading.
 */
public final class ApiClient {

    /**
     * Base address of the Spring backend.
     *
     * 10.0.2.2 is a special alias that the Android EMULATOR maps to "localhost" of the
     * host PC, so this reaches the backend running on your machine at port 8080.
     * Testing on a PHYSICAL phone? Change this to your PC's LAN IP
     * (e.g. "http://192.168.0.12:8080/") and add that IP to network_security_config.xml.
     */
    public static final String BASE_URL = "http://10.0.2.2:8080/";

    private static Retrofit retrofit;

    private ApiClient() {
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // BASIC logs method/url/status (not bodies, so passwords stay out of logcat).
            // Bump to Level.BODY temporarily if you need to inspect the JSON while debugging.
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static <T> T create(Class<T> service) {
        return getRetrofit().create(service);
    }
}
