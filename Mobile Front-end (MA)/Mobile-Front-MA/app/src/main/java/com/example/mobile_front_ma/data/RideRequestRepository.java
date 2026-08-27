package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.RideRequestApi;
import com.example.mobile_front_ma.models.dto.CreateRideRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideRequestRepository {

    private final RideRequestApi api;

    public RideRequestRepository(Context context) {
        api = com.example.mobile_front_ma.data.network.ApiClient
                .createAuthenticated(context, RideRequestApi.class);
    }

    public void createRideRequest(
            CreateRideRequest request,
            ApiCallback<Void> callback) {

        api.createRideRequest(request).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response) {

                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                    return;
                }

                callback.onError(errorMessage(response));
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable t) {

                callback.onError(
                        "Cannot reach the server. Make sure the backend is running."
                );
            }
        });
    }

    private String errorMessage(Response<?> response) {

        switch (response.code()) {

            case 400:
                return "The ride request is invalid.";

            case 401:
                return "You must be logged in to order a ride.";

            case 403:
                return "You are not allowed to order a ride.";

            default:
                return "Could not order the ride (error "
                        + response.code() + ").";
        }
    }
}