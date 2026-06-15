package com.example.mobile_front_ma.models;

/**
 * A named location in Novi Sad: a geocoding suggestion the user can pick as the ride's
 * start or destination. {@link #toString()} returns the label so an ArrayAdapter shows
 * it directly in the autocomplete dropdown.
 */
public class Place {

    private final String label;
    private final double lat;
    private final double lon;

    public Place(String label, double lat, double lon) {
        this.label = label;
        this.lat = lat;
        this.lon = lon;
    }

    public String getLabel() {
        return label;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public String toString() {
        return label;
    }
}
