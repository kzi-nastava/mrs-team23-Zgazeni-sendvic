package com.example.mobile_front_ma.models;

public class Ride {

    private Long rideId;
    private String pickup;
    private String destination;
    private String fare;
    private String date;
    private String status;

    public Ride(
            Long rideId,
            String pickup,
            String destination,
            String fare,
            String date,
            String status
    ) {
        this.rideId = rideId;
        this.pickup = pickup;
        this.destination = destination;
        this.fare = fare;
        this.date = date;
        this.status = status;
    }

    public Long getRideId() {
        return rideId;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDestination() {
        return destination;
    }

    public String getFare() {
        return fare;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
}