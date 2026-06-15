package com.example.mobile_front_ma.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.mobile_front_ma.data.network.ApiCallback;
import com.example.mobile_front_ma.data.network.ApiClient;
import com.example.mobile_front_ma.data.network.HistoryApi;
import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.models.dto.RideDetails;
import com.example.mobile_front_ma.models.dto.RideHistoryItem;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Data layer for ride history (spec 2.9.1 registered user / 2.9.3 administrator) and the
 * "order the same route again" action. Talks to the JWT-protected HOR endpoints and reports
 * plain results back through {@link ApiCallback}, keeping Retrofit out of the UI layer.
 */
public class HistoryRepository {

    private static final MediaType JSON = MediaType.parse("application/json");

    private final HistoryApi api;

    public HistoryRepository(Context context) {
        this.api = ApiClient.createAuthenticated(context, HistoryApi.class);
    }

    public void getUserHistory(int page, int size, String sort, String fromDate, String toDate,
                               ApiCallback<PageResponse<RideHistoryItem>> callback) {
        api.getUserHistory(page, size, sort, fromDate, toDate).enqueue(pageCallback(callback));
    }

    public void getAdminHistory(long targetId, int page, int size, String sort,
                                String fromDate, String toDate,
                                ApiCallback<PageResponse<RideHistoryItem>> callback) {
        api.getAdminHistory(targetId, page, size, sort, fromDate, toDate)
                .enqueue(pageCallback(callback));
    }

    public void getUserRideDetails(long rideId, ApiCallback<RideDetails> callback) {
        api.getUserRideDetails(rideId).enqueue(detailsCallback(callback));
    }

    public void getAdminRideDetails(long rideId, ApiCallback<RideDetails> callback) {
        api.getAdminRideDetails(rideId).enqueue(detailsCallback(callback));
    }

    /** Re-order the route. Pass {@code scheduledTime} (ISO datetime) for "later", or null for "now". */
    public void reorder(long rideId, String scheduledTime, ApiCallback<Void> callback) {
        RequestBody emptyBody = RequestBody.create(JSON, "{}");
        api.reorder(rideId, scheduledTime, emptyBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError(errorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError(networkError());
            }
        });
    }

    private Callback<PageResponse<RideHistoryItem>> pageCallback(
            ApiCallback<PageResponse<RideHistoryItem>> callback) {
        return new Callback<PageResponse<RideHistoryItem>>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<RideHistoryItem>> call,
                                   @NonNull Response<PageResponse<RideHistoryItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(errorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<RideHistoryItem>> call,
                                  @NonNull Throwable t) {
                callback.onError(networkError());
            }
        };
    }

    private Callback<RideDetails> detailsCallback(ApiCallback<RideDetails> callback) {
        return new Callback<RideDetails>() {
            @Override
            public void onResponse(@NonNull Call<RideDetails> call,
                                   @NonNull Response<RideDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(errorMessage(response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RideDetails> call, @NonNull Throwable t) {
                callback.onError(networkError());
            }
        };
    }

    private String errorMessage(int code) {
        if (code == 401 || code == 403) {
            return "Your session has expired. Please log in again.";
        }
        if (code == 404) {
            return "Nothing found for this request.";
        }
        return "Request failed (error " + code + ").";
    }

    private String networkError() {
        return "Cannot reach the server. Make sure the backend is running and the address is correct.";
    }
}
