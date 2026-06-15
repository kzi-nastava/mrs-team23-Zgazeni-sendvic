package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.AccountListItem;
import com.example.mobile_front_ma.models.dto.PageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Admin account directory (backend AccountController). Lets an administrator search for the
 * user/driver whose ride history they want to inspect (spec 2.9.3). Behind JWT/ADMIN auth.
 */
public interface AccountApi {

    @GET("api/account/all")
    Call<PageResponse<AccountListItem>> getAll(
            @Query("q") String query,
            @Query("type") String type,        // "User" / "Driver" / null for all
            @Query("page") int page,
            @Query("size") int size);
}
