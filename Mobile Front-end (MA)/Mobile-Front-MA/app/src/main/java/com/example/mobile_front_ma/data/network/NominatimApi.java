package com.example.mobile_front_ma.data.network;

import java.util.List;

import com.example.mobile_front_ma.models.dto.NominatimPlace;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * OpenStreetMap Nominatim geocoding (address -> coordinates) used to power the
 * start/destination autocomplete. The viewbox + bounded params keep results inside
 * Novi Sad, since the service only operates there.
 */
public interface NominatimApi {

    @GET("search")
    Call<List<NominatimPlace>> search(
            @Query("q") String query,
            @Query("format") String format,
            @Query("addressdetails") int addressDetails,
            @Query("limit") int limit,
            @Query("countrycodes") String countryCodes,
            @Query("viewbox") String viewbox,
            @Query("bounded") int bounded,
            @Query("accept-language") String language);
}
