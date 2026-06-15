package com.example.mobile_front_ma.models;

/**
 * Plain latitude/longitude pair used across the data and UI layers so the data layer
 * never has to depend on a specific map library type (osmdroid's GeoPoint).
 */
public class LatLng {

    private final double lat;
    private final double lon;

    public LatLng(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
