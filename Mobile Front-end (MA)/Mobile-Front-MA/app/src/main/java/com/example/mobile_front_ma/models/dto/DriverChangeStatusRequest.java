package com.example.mobile_front_ma.models.dto;

/**
 * Body for PUT /api/driver/changeStatus (matches backend DriverChangeStatusDTO).
 *
 * Toggles a driver between active (available) and inactive. The backend currently
 * identifies the driver by {@code email}; {@code token} is sent for forward
 * compatibility once the server starts reading it from the request.
 */
public class DriverChangeStatusRequest {

    private String token;
    private String email;
    private boolean toState;

    public DriverChangeStatusRequest(String token, String email, boolean toState) {
        this.token = token;
        this.email = email;
        this.toState = toState;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public boolean isToState() {
        return toState;
    }
}
