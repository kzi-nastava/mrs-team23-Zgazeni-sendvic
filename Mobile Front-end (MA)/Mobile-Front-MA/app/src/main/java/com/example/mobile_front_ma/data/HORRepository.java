package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.RideApi;
import com.example.mobile_front_ma.models.Ride;
import com.example.mobile_front_ma.models.dto.DriverRideResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HORRepository {

    private final RideApi api;

    public HORRepository(Context context) {
        api = ApiClient.createAuthenticated(
                context,
                RideApi.class
        );
    }

    public void getDriverRides(
            ApiCallback<List<DriverRideResponse>> callback
    ) {

        api.getDriverRides().enqueue(
                new Callback<List<DriverRideResponse>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<DriverRideResponse>> call,
                            @NonNull Response<List<DriverRideResponse>> response
                    ) {

                        if (response.isSuccessful() &&
                                response.body() != null) {

                            callback.onSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    "Could not load driver rides."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<DriverRideResponse>> call,
                            @NonNull Throwable t
                    ) {

                        callback.onError(
                                "Cannot reach the server."
                        );
                    }
                }
        );
    }
}