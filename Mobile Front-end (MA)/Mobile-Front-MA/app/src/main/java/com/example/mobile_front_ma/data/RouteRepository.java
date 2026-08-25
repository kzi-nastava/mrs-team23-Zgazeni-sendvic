package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.RouteApi;
import com.example.mobile_front_ma.models.dto.RouteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteRepository {

    private final RouteApi api;

    public RouteRepository(Context context) {
        api = ApiClient.createAuthenticated(
                context,
                RouteApi.class
        );
    }

    public void getFavorites(
            ApiCallback<List<RouteResponse>> callback
    ) {
        api.getFavorites().enqueue(
                new Callback<List<RouteResponse>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<RouteResponse>> call,
                            @NonNull Response<List<RouteResponse>> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {
                            callback.onError(errorMessage(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<RouteResponse>> call,
                            @NonNull Throwable t
                    ) {
                        callback.onError(networkError());
                    }
                }
        );
    }

    public void saveFromRide(
            long rideId,
            ApiCallback<RouteResponse> callback
    ) {
        api.saveFromRide(rideId).enqueue(
                new Callback<RouteResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<RouteResponse> call,
                            @NonNull Response<RouteResponse> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(response.body());

                        } else {
                            callback.onError(errorMessage(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<RouteResponse> call,
                            @NonNull Throwable t
                    ) {
                        callback.onError(networkError());
                    }
                }
        );
    }

    public void deleteFavorite(
            long routeId,
            ApiCallback<Void> callback
    ) {
        api.deleteFavorite(routeId).enqueue(
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Void> call,
                            @NonNull Response<Void> response
                    ) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(errorMessage(response.code()));
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Void> call,
                            @NonNull Throwable t
                    ) {
                        callback.onError(networkError());
                    }
                }
        );
    }

    private String errorMessage(int code) {
        if (code == 401 || code == 403) {
            return "Your session has expired. Please log in again.";
        }

        if (code == 404) {
            return "Favorite route not found.";
        }

        return "Request failed (error " + code + ").";
    }

    private String networkError() {
        return "Cannot reach the server. Make sure the backend is running "
                + "and the address is correct.";
    }
}