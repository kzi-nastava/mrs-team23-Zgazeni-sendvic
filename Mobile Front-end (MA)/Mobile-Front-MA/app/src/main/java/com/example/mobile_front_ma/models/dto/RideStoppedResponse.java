package com.example.mobile_front_ma.models.dto;

import java.util.List;

/**
 * Response from PUT /api/ride-tracking/stop/{rideID} (matches backend RideStoppedDTO):
 * the stopped ride's id, the recalculated price for the distance actually driven,
 * and the trimmed list of destinations.
 */
public class RideStoppedResponse {

    private Long rideID;
    private double newPrice;
    private List<LocationDto> updatedDestinations;

    public Long getRideID() {
        return rideID;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public List<LocationDto> getUpdatedDestinations() {
        return updatedDestinations;
    }
}
