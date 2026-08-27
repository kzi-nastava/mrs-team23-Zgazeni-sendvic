package com.example.mobile_front_ma.data.network;

import android.content.Context;

import com.example.mobile_front_ma.BuildConfig;
import com.example.mobile_front_ma.data.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Single shared Retrofit instance for the whole app.
 */
public final class ApiClient {

    public static final String BASE_URL = BuildConfig.BACKEND_URL;

    private static Retrofit retrofit;
    private static Retrofit authRetrofit;

    private ApiClient() {
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
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

    public static Retrofit getAuthRetrofit(Context context) {
        if (authRetrofit == null) {
            Context appContext = context.getApplicationContext();
            SessionManager session = new SessionManager(appContext);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String token = session.getToken();
                        if (token == null || token.isEmpty()) {
                            return chain.proceed(chain.request());
                        }
                        return chain.proceed(chain.request().newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build());
                    })
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            authRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit;
    }

    public static <T> T createAuthenticated(Context context, Class<T> service) {
        return getAuthRetrofit(context).create(service);
    }
}
