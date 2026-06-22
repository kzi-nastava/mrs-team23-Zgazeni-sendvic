package com.example.mobile_front_ma.models.dto;

/**
 * Response of POST /api/ride-PANIC/{rideID} (spec 2.6.3 – PANIC button).
 *
 * Mirrors the backend {@code PanicNotificationDTO}: the registered panic alert, including who
 * raised it and when. The screen only needs to know the call succeeded, but the fields are
 * kept so the confirmation can show who/when if needed.
 */
public class PanicResponse {

    public Long id;
    public Long callerId;
    public String callerName;
    public Long rideId;
    public String createdAt;   // ISO datetime
    public boolean resolved;
    public String resolvedAt;  // ISO datetime, null until an admin resolves it
}
