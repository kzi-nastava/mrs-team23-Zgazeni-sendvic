package com.example.mobile_front_ma.models.dto;

import com.google.gson.annotations.SerializedName;

public class RideNoteDTO {
    @SerializedName("rideId")
    public Long rideId;
    
    @SerializedName("note")
    public String note;

    public RideNoteDTO() {}

    public RideNoteDTO(Long rideId, String note) {
        this.rideId = rideId;
        this.note = note;
    }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
