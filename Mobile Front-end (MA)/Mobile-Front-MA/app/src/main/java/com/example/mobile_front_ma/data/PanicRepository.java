package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.PanicApi;
import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.models.dto.PanicResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Data layer for the admin panic-notifications screen (spec 2.6.3): lists every panic alert
 * the backend has stored and resolves them. Keeps Retrofit out of the ViewModel/UI.
 */
public class PanicRepository {

    /** Default server ordering for the list (newest alert first). */
    private static final String SORT_NEWEST_FIRST = "createdAt,desc";

    private final PanicApi api;

    public PanicRepository(Context context) {
        this.api = ApiClient.createAuthenticated(context, PanicApi.class);
    }

    public void getPanics(int page, int size, ApiCallback<PageResponse<PanicResponse>> callback) {
        api.retrieveAll(page, size, SORT_NEWEST_FIRST)
                .enqueue(new Callback<PageResponse<PanicResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<PageResponse<PanicResponse>> call,
                                           @NonNull Response<PageResponse<PanicResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(errorMessage(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PageResponse<PanicResponse>> call,
                                          @NonNull Throwable t) {
                        callback.onError(networkError());
                    }
                });
    }

    public void resolve(long panicId, ApiCallback<PanicResponse> callback) {
        api.resolve(panicId).enqueue(new Callback<PanicResponse>() {
            @Override
            public void onResponse(@NonNull Call<PanicResponse> call,
                                   @NonNull Response<PanicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(errorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PanicResponse> call, @NonNull Throwable t) {
                callback.onError(networkError());
            }
        });
    }

    private String errorMessage(int code) {
        switch (code) {
            case 401:
            case 403:
                return "Your session has expired, or this isn't an admin account.";
            case 404:
                return "That panic alert no longer exists.";
            default:
                return "Request failed (error " + code + ").";
        }
    }

    private String networkError() {
        return "Cannot reach the server. Make sure the backend is running.";
    }
}
