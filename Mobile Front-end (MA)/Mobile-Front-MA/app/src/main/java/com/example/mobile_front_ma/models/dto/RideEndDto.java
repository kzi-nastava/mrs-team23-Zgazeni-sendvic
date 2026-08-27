package com.example.mobile_front_ma.models.dto;

import com.google.gson.annotations.SerializedName;

public class RideEndDto {
    @SerializedName("rideId")
    public Long rideId;
    
    @SerializedName("price")
    public Double price;
    
    @SerializedName("paid")
    public boolean paid;
    
    @SerializedName("ended")
    public boolean ended;

    public RideEndDto(Long rideId, Double price, boolean paid, boolean ended) {
        this.rideId = rideId;
        this.price = price;
        this.paid = paid;
        this.ended = ended;
    }
}
