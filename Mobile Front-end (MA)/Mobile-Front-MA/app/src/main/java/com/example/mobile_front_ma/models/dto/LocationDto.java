package com.example.mobile_front_ma.models.dto;

/**
 * A single map point as returned by the backend ({@code Location}: latitude/longitude).
 */
public class LocationDto {

    public Double latitude;
    public Double longitude;

    public LocationDto() {}

    public LocationDto(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean isValid() {
        return latitude != null && longitude != null;
    }
}
