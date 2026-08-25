package com.example.mobile_front_ma.models.dto;

import com.google.gson.annotations.SerializedName;

public class RideSummary {

    @SerializedName("rideCount")
    private int rideCount;

    @SerializedName("totalDistanceKm")
    private double totalDistanceKm;

    @SerializedName("totalDurationMinutes")
    private long totalDurationMinutes;

    @SerializedName("totalPrice")
    private double totalPrice;

    public int getRideCount() {
        return rideCount;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public long getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}