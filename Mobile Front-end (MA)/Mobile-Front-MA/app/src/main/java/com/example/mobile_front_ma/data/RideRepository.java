package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.RideApi;
import com.example.mobile_front_ma.models.dto.RideStopRequest;
import com.example.mobile_front_ma.models.dto.RideStoppedResponse;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Talks to the ride endpoints (RideController) over the authenticated client.
 */
public class RideRepository {

    private final RideApi api;

    public RideRepository(Context context) {
        this.api = ApiClient.createAuthenticated(context, RideApi.class);
    }

    /** Spec 2.6.5 – stop a ride in progress (PUT /api/ride-tracking/stop/{rideID}). */
    public void stopRide(long rideId, RideStopRequest request, ApiCallback<RideStoppedResponse> callback) {
        api.stopRide(rideId, request).enqueue(new Callback<RideStoppedResponse>() {
            @Override
            public void onResponse(@NonNull Call<RideStoppedResponse> call,
                                   @NonNull Response<RideStoppedResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(errorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideStoppedResponse> call, @NonNull Throwable t) {
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
        return "Could not stop the ride (error " + response.code() + ").";
    }
}
