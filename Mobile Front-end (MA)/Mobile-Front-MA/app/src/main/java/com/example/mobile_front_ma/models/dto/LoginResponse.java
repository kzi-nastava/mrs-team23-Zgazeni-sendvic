package com.example.mobile_front_ma.models.dto;

/**
 * Response from POST /api/auth/login (matches backend LoginRequestedDTO).
 */
public class LoginResponse {

    private String token;
    private int expiresIn;
    private AccountResponse user;

    public String getToken() {
        return token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public AccountResponse getUser() {
        return user;
    }
}
