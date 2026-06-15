package com.example.mobile_front_ma.models.dto;

/**
 * One account in the admin directory (backend AccountAdminViewDTO), used so an
 * administrator can pick whose ride history to inspect (spec 2.9.3).
 */
public class AccountListItem {

    public Long id;
    public String email;
    public String name;
    public String lastName;
    public String phoneNumber;
    public String address;
    public boolean confirmed;
    public boolean banned;
    public String accountType;   // "User", "Driver", "Admin"

    public String fullName() {
        String first = name == null ? "" : name;
        String last = lastName == null ? "" : lastName;
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? (email == null ? "" : email) : combined;
    }
}
