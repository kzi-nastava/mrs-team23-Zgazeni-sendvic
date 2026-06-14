package com.example.mobile_front_ma.models.dto;

/**
 * Body for POST /api/auth/login (matches backend LoginRequestDTO).
 */
public class LoginRequest {

    private final String email;
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
