package com.example.mobile_front_ma.models.dto;

public class ActiveRideDTO {
    public Long id;
    public LocationDto origin;
    public LocationDto destination;
    public String departureTime;
    public String arrivalTime;
    public boolean panic;
    public String status;
    public double price;
    public String driverEmail;
    public String driverFirstName;
    public String date;

    public ActiveRideDTO() {}
}
