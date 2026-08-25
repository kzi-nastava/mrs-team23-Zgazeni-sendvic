package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.OsrmRouteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OsrmOrderingApi {

    @GET("route/v1/driving/{coordinates}")
    Call<OsrmRouteResponse> route(
            @Path(value = "coordinates", encoded = true) String coordinates,
            @Query("overview") String overview,
            @Query("geometries") String geometries
    );
}