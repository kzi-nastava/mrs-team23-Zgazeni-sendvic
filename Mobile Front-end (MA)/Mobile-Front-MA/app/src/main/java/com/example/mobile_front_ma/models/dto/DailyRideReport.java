package com.example.mobile_front_ma.models.dto;

import com.google.gson.annotations.SerializedName;

public class DailyRideReport {

    @SerializedName("date")
    private String date;

    @SerializedName("rideCount")
    private int rideCount;

    @SerializedName("totalDistanceKm")
    private double totalDistanceKm;

    @SerializedName("totalPrice")
    private double totalPrice;

    public String getDate() {
        return date;
    }

    public int getRideCount() {
        return rideCount;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}