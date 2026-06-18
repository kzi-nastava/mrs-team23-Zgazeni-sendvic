package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.RideCancelRequest;
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

    /**
     * Spec 2.5 – cancel a scheduled ride. The backend decides whether the caller is allowed
     * (passenger within the 10-minute window, or an administrator) from the JWT, so the body
     * only carries an optional reason. Returns the cancellation record, which we ignore.
     */
    @PUT("api/ride-cancel/{rideID}")
    Call<Void> cancelRide(@Path("rideID") long rideID, @Body RideCancelRequest body);
}
