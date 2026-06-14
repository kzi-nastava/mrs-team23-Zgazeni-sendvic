package com.example.mobile_front_ma.models.dto;

/**
 * Body for POST /api/auth/register (matches backend RegisterRequestDTO).
 * Field names must match the server JSON, so they are intentionally kept identical.
 */
public class RegisterRequest {

    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String phoneNum;
    private final String pictUrl;

    public RegisterRequest(String email, String password, String firstName, String lastName,
                           String address, String phoneNum, String pictUrl) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phoneNum = phoneNum;
        this.pictUrl = pictUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getPictUrl() {
        return pictUrl;
    }
}
