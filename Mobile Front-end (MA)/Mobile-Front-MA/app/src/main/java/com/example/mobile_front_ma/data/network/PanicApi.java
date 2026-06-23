package com.example.mobile_front_ma.data.network;

import com.example.mobile_front_ma.models.dto.PageResponse;
import com.example.mobile_front_ma.models.dto.PanicResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Admin-facing panic-notification endpoints (backend PanicNotificationController, spec 2.6.3).
 * Both are behind ROLE_ADMIN, so they go through the authenticated client.
 */
public interface PanicApi {

    /**
     * All panic alerts (resolved and unresolved), newest first, paged. This is the
     * "stored in the DB so admins can react later" list the spec asks for: an admin who was
     * offline when an alert fired still sees it here.
     */
    @GET("api/panic-notifications/retrieve-all")
    Call<PageResponse<PanicResponse>> retrieveAll(@Query("page") int page,
                                                  @Query("size") int size,
                                                  @Query("sort") String sort);

    /** Mark a panic alert resolved. {@code id} is the panic-notification id (not the ride id). */
    @POST("api/panic-notifications/resolve/{id}")
    Call<PanicResponse> resolve(@Path("id") long id);
}
