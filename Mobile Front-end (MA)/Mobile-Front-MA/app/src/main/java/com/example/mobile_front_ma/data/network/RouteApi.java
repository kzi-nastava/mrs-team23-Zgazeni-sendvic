package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.RouteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RouteApi {

    @GET("api/routes/favorites")
    Call<List<RouteResponse>> getFavorites();

    @POST("api/routes/favorites/from-ride/{rideId}")
    Call<RouteResponse> saveFromRide(
            @Path("rideId") long rideId
    );

    @DELETE("api/routes/favorites/{routeId}")
    Call<Void> deleteFavorite(
            @Path("routeId") long routeId
    );
}