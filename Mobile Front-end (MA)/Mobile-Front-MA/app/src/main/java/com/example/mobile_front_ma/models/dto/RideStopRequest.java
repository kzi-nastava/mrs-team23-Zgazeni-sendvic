package com.example.mobile_front_ma.models.dto;

import java.util.List;

/**
 * Body for PUT /api/ride-tracking/stop/{rideID} (matches backend RideStopDTO).
 *
 * Spec 2.6.5 – stopping a ride that is in progress. {@code passedLocations} is the
 * part of the route already driven and {@code currentTime} is the moment the driver
 * pressed stop (ISO-8601 local date-time, e.g. "2026-06-15T12:30:00").
 */
public class RideStopRequest {

    private List<LocationDto> passedLocations;
    private String currentTime;

    public RideStopRequest(List<LocationDto> passedLocations, String currentTime) {
        this.passedLocations = passedLocations;
        this.currentTime = currentTime;
    }

    public List<LocationDto> getPassedLocations() {
        return passedLocations;
    }

    public String getCurrentTime() {
        return currentTime;
    }
}
