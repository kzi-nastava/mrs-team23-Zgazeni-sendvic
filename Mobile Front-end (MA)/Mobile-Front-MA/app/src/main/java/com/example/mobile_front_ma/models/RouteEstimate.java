package com.example.mobile_front_ma.models;

import java.util.List;

/**
 * Result of a ride estimation (spec 2.1.2): the driving distance, the estimated travel
 * time and the route geometry to draw on the map.
 */
public class RouteEstimate {

    private final double distanceMeters;
    private final double durationSeconds;
    private final List<LatLng> geometry;

    public RouteEstimate(double distanceMeters, double durationSeconds, List<LatLng> geometry) {
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
