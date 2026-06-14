package com.example.mobile_front_ma.models.dto;

/**
 * Body for POST /api/auth/reset-password: the emailed code plus the new password,
 * scoped to the account by email. Field names match the backend DTO.
 */
public class ResetPasswordRequest {

    private final String email;
    private final String code;
    private final String newPassword;

    public ResetPasswordRequest(String email, String code, String newPassword) {
        this.email = email;
        this.code = code;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
