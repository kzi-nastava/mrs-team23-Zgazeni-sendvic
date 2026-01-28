package com.example.mobile_front_ma.models;

public class Ride {
    private final String pickup;
    private final String destination;
    private final String fare;
    private final String date;

    public Ride(String pickup, String destination, String fare, String date) {
        this.pickup = pickup;
        this.destination = destination;
        this.fare = fare;
        this.date = date;
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
}
