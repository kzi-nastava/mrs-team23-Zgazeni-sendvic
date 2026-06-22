package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.RideApi;
import com.example.mobile_front_ma.models.dto.PanicResponse;
import com.example.mobile_front_ma.models.dto.RideCancelRequest;
import com.example.mobile_front_ma.models.dto.RideStopRequest;
import com.example.mobile_front_ma.models.dto.RideStoppedResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    /**
     * Spec 2.5 – cancel a scheduled ride (PUT /api/ride-cancel/{rideID}). {@code reason} may be
     * null; it is only meaningful for a driver cancelling, which this app doesn't do.
     */
    public void cancelRide(long rideId, String reason, ApiCallback<Void> callback) {
        api.cancelRide(rideId, new RideCancelRequest(reason)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(cancelErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError("Cannot reach the server. Make sure the backend is running.");
            }
        });
    }

    /**
     * Spec 2.6.3 – raise the PANIC alarm on a ride in progress (POST /api/ride-PANIC/{rideID}).
     * The backend authorizes the caller (driver or passenger of an ACTIVE ride) from the JWT,
     * so there is no body.
     */
    public void panicRide(long rideId, ApiCallback<PanicResponse> callback) {
        api.panicRide(rideId).enqueue(new Callback<PanicResponse>() {
            @Override
            public void onResponse(@NonNull Call<PanicResponse> call,
                                   @NonNull Response<PanicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(panicErrorMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PanicResponse> call, @NonNull Throwable t) {
                callback.onError("Cannot reach the server. Make sure the backend is running.");
            }
        });
    }

    private String panicErrorMessage(Response<?> response) {
        // Prefer the backend's own reason (e.g. "Only active rides can be panicked",
        // "Panic has already been activated for this ride").
        String backendMessage = backendMessage(response);
        if (backendMessage != null && !backendMessage.isEmpty()) {
            return backendMessage;
        }
        switch (response.code()) {
            case 400:
                return "Panic can't be raised for this ride right now.";
            case 401:
            case 403:
                return "You're not allowed to raise panic on this ride.";
            case 404:
                return "Ride not found.";
            default:
                return "Could not send the panic alert (error " + response.code() + ").";
        }
    }

    private String cancelErrorMessage(Response<?> response) {
        // Prefer the backend's own reason (e.g. "Ride is not scheduled", "Too late to cancel").
        String backendMessage = backendMessage(response);
        if (backendMessage != null && !backendMessage.isEmpty()) {
            return backendMessage;
        }
        switch (response.code()) {
            case 400:
            case 406:
            case 409:
                return "This ride can no longer be canceled.";
            case 403:
                return "You're not allowed to cancel this ride.";
            case 404:
                return "Ride not found.";
            default:
                return "Could not cancel the ride (error " + response.code() + ").";
        }
    }

    private String errorMessage(Response<?> response) {
        // Prefer the backend's own reason when it sends one.
        String backendMessage = backendMessage(response);
        if (backendMessage != null && !backendMessage.isEmpty()) {
            return backendMessage;
        }
        // Otherwise (e.g. Spring's default 403 body carries no message) fall back to a
        // clear, ride-specific message instead of a bare "403 Forbidden".
        switch (response.code()) {
            case 400:
                return "This ride can't be stopped – it may have already finished.";
            case 403:
                return "You are not the driver of this ride.";
            case 404:
                return "Ride not found.";
            default:
                return "Could not stop the ride (error " + response.code() + ").";
        }
    }

    /** Pulls a human-readable reason out of the backend error body, or null if there isn't one. */
    private String backendMessage(Response<?> response) {
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody == null) {
                return null;
            }
            String body = errorBody.string().trim();
            if (body.isEmpty()) {
                return null;
            }
            // Most error bodies are JSON like {"status":...,"error":...,"message":"..."}.
            try {
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                if (!json.has("message") || json.get("message").isJsonNull()) {
                    return null;
                }
                String message = json.get("message").getAsString().trim();
                // ResponseStatusException prefixes its reason, e.g. 400 BAD_REQUEST "Ride is
                // not active" – keep only the readable part inside the quotes when present.
                int firstQuote = message.indexOf('"');
                int lastQuote = message.lastIndexOf('"');
                if (firstQuote >= 0 && lastQuote > firstQuote) {
                    message = message.substring(firstQuote + 1, lastQuote).trim();
                }
                return message.isEmpty() ? null : message;
            } catch (RuntimeException notJson) {
                // Not JSON – treat the raw body as the message.
                return body.replace("\"", "");
            }
        } catch (IOException ignored) {
            return null;
        }
    }
}
