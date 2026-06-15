package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.AccountApi;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.models.dto.AccountListItem;
import com.example.mobile_front_ma.models.dto.PageResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Data layer for the admin account directory: searching users/drivers so an administrator
 * can pick whose ride history to inspect (spec 2.9.3). Behind JWT/ADMIN auth.
 */
public class AccountRepository {

    private static final int PAGE_SIZE = 30;

    private final AccountApi api;

    public AccountRepository(Context context) {
        this.api = ApiClient.createAuthenticated(context, AccountApi.class);
    }

    public void search(String query, ApiCallback<PageResponse<AccountListItem>> callback) {
        api.getAll(query, null, 0, PAGE_SIZE).enqueue(new Callback<PageResponse<AccountListItem>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<AccountListItem>> call,
                                   @NonNull Response<PageResponse<AccountListItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 401 || response.code() == 403) {
                    callback.onError("Your session has expired. Please log in again.");
                } else {
                    callback.onError("Couldn't load accounts (error " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<AccountListItem>> call,
                                  @NonNull Throwable t) {
                callback.onError("Cannot reach the server. Make sure the backend is running.");
            }
        });
    }
}
