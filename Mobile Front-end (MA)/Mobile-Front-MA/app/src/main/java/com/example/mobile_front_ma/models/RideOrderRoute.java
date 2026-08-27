package com.example.mobile_front_ma.models;

import java.util.List;

public class RideOrderRoute {

    private final double distanceMeters;
    private final double durationSeconds;
    private final List<LatLng> geometry;

    public RideOrderRoute(
            double distanceMeters,
            double durationSeconds,
            List<LatLng> geometry) {

        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.geometry = geometry;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public List<LatLng> getGeometry() {
        return geometry;
    }
}