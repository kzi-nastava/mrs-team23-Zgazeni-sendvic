package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.AccountListItem;
import com.example.mobile_front_ma.models.dto.BanAccountRequest;
import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.models.dto.GetAccountDTO;
import com.example.mobile_front_ma.models.dto.UpdateAccountDTO;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    @GET("api/account/me")
    Call<GetAccountDTO> getMe();

    @PUT("api/account/me/change-request")
    Call<ResponseBody> updateMe(@Body UpdateAccountDTO request);

    @PUT("api/account/ban/{id}")
    Call<ResponseBody> banAccount(
            @Path("id") Long accountId,
            @Body BanAccountRequest request
    );
}
