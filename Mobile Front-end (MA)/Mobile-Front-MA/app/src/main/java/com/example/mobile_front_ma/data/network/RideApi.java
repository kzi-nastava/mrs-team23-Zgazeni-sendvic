package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.RideStopRequest;
import com.example.mobile_front_ma.models.dto.RideStoppedResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Ride lifecycle endpoints (backend RideController). Behind JWT/DRIVER auth.
 */
public interface RideApi {

    /** Spec 2.6.5 – stop a ride that is currently in progress. */
    @PUT("api/ride-tracking/stop/{rideID}")
    Call<RideStoppedResponse> stopRide(@Path("rideID") long rideID, @Body RideStopRequest body);
}
