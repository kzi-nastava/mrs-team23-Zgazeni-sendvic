package com.example.mobile_front_ma.models.dto;

/**
 * Driver / passenger summary shown in the ride detail screen
 * (backend HORAccountDetailsDTO).
 */
public class AccountDetails {

    public Long accountId;
    public String email;
    public String firstName;
    public String lastName;

    public String fullName() {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        return (first + " " + last).trim();
    }
}
