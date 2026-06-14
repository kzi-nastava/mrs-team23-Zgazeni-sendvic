package com.example.mobile_front_ma.models.dto;

/**
 * Response from POST /api/auth/register. The server returns a one-time token that
 * can be used to upload the profile picture right after registering.
 */
public class RegisterResponse {

    private String pictureToken;

    public String getPictureToken() {
        return pictureToken;
    }
}
