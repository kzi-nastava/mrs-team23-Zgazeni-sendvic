package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.models.dto.RideDetails;
import com.example.mobile_front_ma.models.dto.RideHistoryItem;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Ride history endpoints exposed by the Spring backend (HORController) plus the
 * "order the same route again" action (RideRequestController). All of these are behind
 * JWT auth, so build this through {@link ApiClient#createAuthenticated}.
 */
public interface HistoryApi {

    /** Registered user's own history (spec 2.9.1). */
    @GET("api/HOR/user")
    Call<PageResponse<RideHistoryItem>> getUserHistory(
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort,        // e.g. "creationDate,desc"
            @Query("fromDate") String fromDate, // ISO datetime or null
            @Query("toDate") String toDate);

    /** Detailed view of one of the user's own rides. */
    @GET("api/HOR/user/detailed/{rideId}")
    Call<RideDetails> getUserRideDetails(@Path("rideId") long rideId);

    /** History of any account, viewed by an administrator (spec 2.9.3). */
    @GET("api/HOR/admin/{targetId}")
    Call<PageResponse<RideHistoryItem>> getAdminHistory(
            @Path("targetId") long targetId,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort,
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate);

    /** Detailed view of any ride, viewed by an administrator. */
    @GET("api/HOR/admin/detailed/{rideId}")
    Call<RideDetails> getAdminRideDetails(@Path("rideId") long rideId);

    /**
     * Re-create a ride request from a past ride. {@code fromDate} (ISO datetime) schedules
     * it for later; pass null to order immediately. The endpoint consumes JSON, so we send
     * an empty object body, matching the web client.
     */
    @POST("api/riderequest/ride-reorder/{rideId}")
    Call<Void> reorder(
            @Path("rideId") long rideId,
            @Query("fromDate") String fromDate,
            @Body RequestBody emptyBody);
}
