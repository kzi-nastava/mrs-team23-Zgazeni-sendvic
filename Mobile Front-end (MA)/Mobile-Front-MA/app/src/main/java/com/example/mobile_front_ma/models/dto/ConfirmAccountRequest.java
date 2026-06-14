package com.example.mobile_front_ma.models.dto;

/**
 * Body for POST /api/auth/confirm-account: the emailed activation code, scoped to the
 * account by email. Field names match the backend DTO.
 */
public class ConfirmAccountRequest {

    private final String email;
    private final String code;

    public ConfirmAccountRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}
