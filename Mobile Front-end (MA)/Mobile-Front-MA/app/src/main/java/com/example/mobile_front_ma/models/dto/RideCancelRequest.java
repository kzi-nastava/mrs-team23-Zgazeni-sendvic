package com.example.mobile_front_ma.models.dto;

/**
 * Body for PUT /api/ride-cancel/{rideID} (spec 2.5).
 *
 * {@code reason} is optional for a passenger or an administrator; the backend only requires
 * a reason when the driver of the ride is the one cancelling, which is not a case this app
 * triggers. Sent as JSON; Gson omits the field when null.
 */
public class RideCancelRequest {

    public String reason;

    public RideCancelRequest() {
    }

    public RideCancelRequest(String reason) {
        this.reason = reason;
    }
}
