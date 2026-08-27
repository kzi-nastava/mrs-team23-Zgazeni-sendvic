package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.CreateDriverRequest;
import com.example.mobile_front_ma.models.dto.CreatedDriverResponse;
import com.example.mobile_front_ma.models.dto.DriverChangeStatusRequest;
import com.example.mobile_front_ma.models.dto.DriverStatusResponse;
import com.example.mobile_front_ma.models.dto.RegisterVehicleRequest;
import com.example.mobile_front_ma.models.dto.RegisteredVehicleResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Driver-specific endpoints (backend DriverController). Behind JWT/DRIVER auth.
 */
public interface DriverApi {

    /** Toggle the logged-in driver between active (available) and inactive. */
    @PUT("api/driver/changeStatus")
    Call<DriverStatusResponse> changeStatus(@Body DriverChangeStatusRequest body);

    @POST("api/driver/vehicle")
    Call<RegisteredVehicleResponse> registerVehicle(
            @Body RegisterVehicleRequest body
    );

    @POST("api/driver")
    Call<CreatedDriverResponse> createDriver(
            @Body CreateDriverRequest body
    );
}
