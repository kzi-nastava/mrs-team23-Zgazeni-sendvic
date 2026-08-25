package com.example.mobile_front_ma.models.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RideReport {

    @SerializedName("dailyReports")
    private List<DailyRideReport> dailyReports;

    @SerializedName("summary")
    private RideSummary summary;

    public List<DailyRideReport> getDailyReports() {
        return dailyReports;
    }

    public RideSummary getSummary() {
        return summary;
    }
}