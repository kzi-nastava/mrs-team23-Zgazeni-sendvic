package com.example.mobile_front_ma.models.dto;

/**
 * The "user" object inside the login response (matches backend AccountLoginDTO).
 */
public class AccountResponse {

    private String email;
    private Long userID;
    private String firstName;
    private String lastName;
    private String pictUrl;
    private String role;

    public String getEmail() {
        return email;
    }

    public Long getUserID() {
        return userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPictUrl() {
        return pictUrl;
    }

    public String getRole() {
        return role;
    }
}
