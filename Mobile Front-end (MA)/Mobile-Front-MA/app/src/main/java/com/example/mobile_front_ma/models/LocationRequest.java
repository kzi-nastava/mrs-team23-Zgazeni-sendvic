package com.example.mobile_front_ma.models;

public class LocationRequest {

    private double latitude;
    private double longitude;

    public LocationRequest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
