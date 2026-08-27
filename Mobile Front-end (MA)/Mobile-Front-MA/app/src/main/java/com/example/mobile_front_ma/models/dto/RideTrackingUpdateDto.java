package com.example.mobile_front_ma.models.dto;

import java.util.List;

public class RideTrackingUpdateDto {
    public Long rideId;
    public Long vehicleId;
    public Double currentLatitude;
    public Double currentLongitude;
    public String status;
    public Double price;
    public Object startTime; 
    public Object estimatedEndTime;
    public String timeLeft;
    public List<LocationDTO> route;
    public DriverInfoDTO driver;

    public static class LocationDTO {
        public Double latitude;
        public Double longitude;

        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
    }

    public static class DriverInfoDTO {
        public Long id;
        public String name;
        public String phoneNumber;
    }

    public Long getRideId() { return rideId; }
    public String getStatus() { return status; }
    public List<LocationDTO> getRoute() { return route; }
    public Double getCurrentLatitude() { return currentLatitude; }
    public Double getCurrentLongitude() { return currentLongitude; }
}
