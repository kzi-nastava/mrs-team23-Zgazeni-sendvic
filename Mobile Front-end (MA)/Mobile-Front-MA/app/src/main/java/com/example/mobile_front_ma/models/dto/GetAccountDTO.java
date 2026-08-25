package com.example.mobile_front_ma.models.dto;

public class GetAccountDTO {

    private Long id;
    private String email;
    private String password;
    private String name;
    private String lastName;
    private String address;
    private String phoneNumber;
    private String imgString;
    private String role;
    private Integer totalDrivingHours;

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getImgString() {
        return imgString;
    }

    public String getRole() {
        return role;
    }

    public Integer getTotalDrivingHours() {
        return totalDrivingHours;
    }
}
