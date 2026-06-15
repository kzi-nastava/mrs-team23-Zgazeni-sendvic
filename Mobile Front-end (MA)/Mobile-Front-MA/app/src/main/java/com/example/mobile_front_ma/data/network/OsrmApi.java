package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.OsrmRouteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * OSRM routing (the OpenStreetMap "directions" service the spec links to). Returns the
 * driving route geometry plus its distance and duration for the ride estimate.
 */
public interface OsrmApi {

    /**
     * @param coordinates "lon,lat;lon,lat" (OSRM uses lon,lat order). Marked encoded so
     *                    the ';' and ',' stay as path syntax instead of being escaped.
     */
    @GET("route/v1/driving/{coordinates}")
    Call<OsrmRouteResponse> route(
            @Path(value = "coordinates", encoded = true) String coordinates,
            @Query("overview") String overview,
            @Query("geometries") String geometries);
}
