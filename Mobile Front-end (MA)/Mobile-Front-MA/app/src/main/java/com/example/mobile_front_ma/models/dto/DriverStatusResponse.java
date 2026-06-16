package com.example.mobile_front_ma.models.dto;

/**
 * Response from PUT /api/driver/changeStatus (matches backend DriverStatusChangedDTO):
 * the driver's true availability after the call plus a human-readable message.
 */
public class DriverStatusResponse {

    private boolean available;
    private String message;

    public boolean isAvailable() {
        return available;
    }

    public String getMessage() {
        return message;
    }
}
