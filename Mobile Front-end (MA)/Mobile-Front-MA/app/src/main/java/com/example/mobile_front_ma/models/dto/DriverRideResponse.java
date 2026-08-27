package com.example.mobile_front_ma.models.dto;

public class DriverRideResponse {

    private Long rideId;
    private String startLocation;
    private String endLocation;
    private String departureTime;
    private String status;
    private Double price;

    public Long getRideId() {
        return rideId;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getStatus() {
        return status;
    }

    public Double getPrice() {
        return price;
    }
}