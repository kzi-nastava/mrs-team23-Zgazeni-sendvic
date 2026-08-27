package com.example.mobile_front_ma.models.dto;

import com.google.gson.annotations.SerializedName;

public class RideNoteRequestDto {
    @SerializedName("rideId")
    public Long rideId;
    
    @SerializedName("note")
    public String note;

    public RideNoteRequestDto() {}

    public RideNoteRequestDto(Long rideId, String note) {
        this.rideId = rideId;
        this.note = note;
    }
}
