package com.example.mobile_front_ma.models.dto;

public class CreateDriverRequest {

    private String email;
    private String password;
    private String name;
    private String lastName;
    private String address;
    private Long vehicleId;
    private String phoneNumber;
    private String imgString;

    public CreateDriverRequest(
            String email,
            String password,
            String name,
            String lastName,
            String address,
            Long vehicleId,
            String phoneNumber,
            String imgString
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
        this.address = address;
        this.vehicleId = vehicleId;
        this.phoneNumber = phoneNumber;
        this.imgString = imgString;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getImgString() {
        return imgString;
    }
}