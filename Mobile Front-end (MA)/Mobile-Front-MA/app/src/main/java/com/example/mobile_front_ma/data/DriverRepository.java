package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.DriverApi;
import com.example.mobile_front_ma.models.dto.DriverChangeStatusRequest;
import com.example.mobile_front_ma.models.dto.DriverStatusResponse;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Talks to the driver endpoints (DriverController) over the authenticated client.
 */
public class DriverRepository {

    private final DriverApi api;

    public DriverRepository(Context context) {
        this.api = ApiClient.createAuthenticated(context, DriverApi.class);
    }

    /**
     * Toggle the logged-in driver active/inactive (PUT /api/driver/changeStatus).
     * The driver is identified server-side from the JWT; the backend replies with the
     * driver's true availability after the call plus a message, both handed back on success.
     */
    public void changeStatus(String token, String email, boolean toState,
                             ApiCallback<DriverStatusResponse> callback) {
        DriverChangeStatusRequest request = new DriverChangeStatusRequest(token, email, toState);
        api.changeStatus(request).enqueue(new Callback<DriverStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<DriverStatusResponse> call,
                                   @NonNull Response<DriverStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(errorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<DriverStatusResponse> call, @NonNull Throwable t) {
                callback.onError("Cannot reach the server. Make sure the backend is running.");
            }
        });
    }

    private String errorMessage(Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                String body = errorBody.string().trim();
                if (!body.isEmpty()) {
                    return body.replace("\"", "");
                }
            }
        } catch (IOException ignored) {
            // fall through to the generic message
        }
        return "Could not change status (error " + response.code() + ").";
    }
}
