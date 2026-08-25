package com.example.mobile_front_ma.models.dto;

public class RideStartRequest {

    private Long rideId;

    public RideStartRequest(Long rideId) {
        this.rideId = rideId;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }
}
