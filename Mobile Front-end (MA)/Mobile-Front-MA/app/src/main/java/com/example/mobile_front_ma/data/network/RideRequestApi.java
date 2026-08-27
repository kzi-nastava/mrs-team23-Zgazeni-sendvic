package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.CreateRideRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RideRequestApi {

    @POST("api/riderequest/create")
    Call<Void> createRideRequest(
            @Body CreateRideRequest request
    );
}