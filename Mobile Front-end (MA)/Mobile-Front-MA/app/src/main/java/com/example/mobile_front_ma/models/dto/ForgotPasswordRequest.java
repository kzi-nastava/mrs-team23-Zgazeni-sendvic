package com.example.mobile_front_ma.models.dto;

/** Body for POST /api/auth/forgot-password: asks the backend to email a reset code. */
public class ForgotPasswordRequest {

    private final String email;

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
