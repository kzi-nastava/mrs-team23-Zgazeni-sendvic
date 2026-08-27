package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.AccountApi;
import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.models.dto.AccountListItem;
import com.example.mobile_front_ma.models.dto.BanAccountRequest;
import com.example.mobile_front_ma.models.dto.GetAccountDTO;
import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.models.dto.UpdateAccountDTO;

import java.io.IOException;

import okhttp3.ResponseBody;
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

    public void getMe(ApiCallback<GetAccountDTO> callback) {
        api.getMe().enqueue(new Callback<GetAccountDTO>() {
            @Override
            public void onResponse(
                    @NonNull Call<GetAccountDTO> call,
                    @NonNull Response<GetAccountDTO> response) {

                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 401 || response.code() == 403) {
                    callback.onError(
                            "Your session has expired. Please log in again."
                    );
                } else {
                    callback.onError(
                            "Couldn't load your profile (error "
                                    + response.code() + ")."
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<GetAccountDTO> call,
                    @NonNull Throwable t) {

                callback.onError(
                        "Cannot reach the server. Make sure the backend is running."
                );
            }
        });
    }

    public void updateMe(
            UpdateAccountDTO dto,
            ApiCallback<String> callback
    ) {
        api.updateMe(dto).enqueue(
                new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<ResponseBody> call,
                            @NonNull Response<ResponseBody> response
                    ) {

                        if (response.isSuccessful()) {

                            String message = "Profile updated successfully.";

                            try {
                                if (response.body() != null) {
                                    String body =
                                            response.body().string();

                                    if (body != null && !body.isBlank()) {
                                        message = body;
                                    }
                                }
                            } catch (IOException ignored) {
                                // The update succeeded, so don't turn this
                                // into an error merely because reading the
                                // optional response body failed.
                            }

                            callback.onSuccess(message);

                        } else if (
                                response.code() == 401 ||
                                        response.code() == 403
                        ) {

                            callback.onError(
                                    "Your session has expired. Please log in again."
                            );

                        } else {

                            callback.onError(
                                    "Couldn't update your profile (error "
                                            + response.code()
                                            + ")."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<ResponseBody> call,
                            @NonNull Throwable t
                    ) {

                        callback.onError(
                                "Cannot reach the server. Make sure the backend is running."
                        );
                    }
                }
        );
    }

    public void banAccount(
            Long accountId,
            String reason,
            ApiCallback<String> callback
    ) {
        BanAccountRequest request = new BanAccountRequest(reason);

        api.banAccount(accountId, request).enqueue(
                new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<ResponseBody> call,
                            @NonNull Response<ResponseBody> response
                    ) {

                        if (response.isSuccessful()) {

                            String message = "Account banned.";

                            try {
                                if (response.body() != null) {
                                    String body = response.body().string();

                                    if (body != null && !body.isBlank()) {
                                        message = body;
                                    }
                                }
                            } catch (IOException ignored) {
                            }

                            callback.onSuccess(message);

                        } else if (response.code() == 401 ||
                                response.code() == 403) {

                            callback.onError(
                                    "You are not authorized to ban this account."
                            );

                        } else if (response.code() == 404) {

                            callback.onError(
                                    "The account could not be found."
                            );

                        } else {

                            String body = readErrorBody(response);

                            if (body != null && !body.isBlank()) {
                                callback.onError(body.trim());
                            } else {
                                callback.onError(
                                        "Couldn't ban account (error "
                                                + response.code() + ")."
                                );
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<ResponseBody> call,
                            @NonNull Throwable t
                    ) {

                        callback.onError(
                                "Cannot reach the server. Make sure the backend is running."
                        );
                    }
                }
        );
    }

    private String readErrorBody(Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            return errorBody != null ? errorBody.string() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
