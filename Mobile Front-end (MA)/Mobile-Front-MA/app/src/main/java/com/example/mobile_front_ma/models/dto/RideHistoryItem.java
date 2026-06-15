package com.example.mobile_front_ma.models.dto;

import java.util.List;

/**
 * One row of the ride history list.
 *
 * The backend returns two slightly different shapes:
 *  - registered user (GET /api/HOR/user): rideID, destinations, beginning, ending, creationTime
 *  - administrator   (GET /api/HOR/admin/{id}): the above plus status, whoCancelled, price, panic
 *
 * Both map onto this class; the admin-only fields stay null/0 for the user view, and the
 * UI shows them only when present.
 */
public class RideHistoryItem {

    public Long rideID;
    public List<LocationDto> destinations;
    public LocationDto arrivingPoint;   // admin payload only
    public LocationDto endingPoint;     // admin payload only
    public String beginning;            // ISO datetime, ride start (may be null)
    public String ending;               // ISO datetime, ride end (may be null)
    public String creationTime;         // ISO datetime, when the ride was created
    public String status;               // admin payload only (FINISHED / CANCELED / ...)
    public Long whoCancelled;           // admin payload only (account id or null)
    public Double price;                // admin payload only
    public Boolean panic;               // admin payload only

    public Long getRideID() {
        return rideID;
    }

    /** First point of the route: prefer the explicit field, fall back to the list. */
    public LocationDto getStart() {
        if (arrivingPoint != null && arrivingPoint.isValid()) {
            return arrivingPoint;
        }
        return (destinations != null && !destinations.isEmpty()) ? destinations.get(0) : null;
    }

    /** Last point of the route. */
    public LocationDto getEnd() {
        if (endingPoint != null && endingPoint.isValid()) {
            return endingPoint;
        }
        return (destinations != null && !destinations.isEmpty())
                ? destinations.get(destinations.size() - 1) : null;
    }

    public boolean isCanceled() {
        return "CANCELED".equalsIgnoreCase(status);
    }

    public boolean hasPanic() {
        return Boolean.TRUE.equals(panic);
    }
}
