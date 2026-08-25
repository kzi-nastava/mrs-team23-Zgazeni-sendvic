package com.example.mobile_front_ma.models.dto;

public class BanAccountRequest {

    private String reason;

    public BanAccountRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}