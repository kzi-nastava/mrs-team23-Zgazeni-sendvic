package com.example.mobile_front_ma.models.dto;

public class RideDriverRatingDTO {
    public Long userId;
    public Long rideId;
    public int driverRating;
    public int vehicleRating;
    public String comment;

    public RideDriverRatingDTO() {}

    public RideDriverRatingDTO(Long userId, Long rideId, int driverRating, int vehicleRating, String comment) {
        this.userId = userId;
        this.rideId = rideId;
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
    }
}
