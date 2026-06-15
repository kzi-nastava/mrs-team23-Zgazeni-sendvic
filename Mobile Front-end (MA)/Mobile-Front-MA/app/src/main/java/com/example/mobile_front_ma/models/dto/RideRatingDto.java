package com.example.mobile_front_ma.models.dto;

/**
 * A driver + vehicle rating with an optional comment (backend RideDriverRatingDTO).
 */
public class RideRatingDto {

    public Long userId;
    public Long rideId;
    public int driverRating;
    public int vehicleRating;
    public String comment;
}
