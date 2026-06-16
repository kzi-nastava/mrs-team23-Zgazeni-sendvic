package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.DriverChangeStatusRequest;
import com.example.mobile_front_ma.models.dto.DriverStatusResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

/**
 * Driver-specific endpoints (backend DriverController). Behind JWT/DRIVER auth.
 */
public interface DriverApi {

    /** Toggle the logged-in driver between active (available) and inactive. */
    @PUT("api/driver/changeStatus")
    Call<DriverStatusResponse> changeStatus(@Body DriverChangeStatusRequest body);
}
