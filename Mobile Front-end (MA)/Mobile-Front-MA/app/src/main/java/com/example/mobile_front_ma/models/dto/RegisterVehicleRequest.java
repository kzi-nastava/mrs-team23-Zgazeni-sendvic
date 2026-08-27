package com.example.mobile_front_ma.models.dto;

public class RegisterVehicleRequest {

    private String model;
    private String registration;
    private String type;
    private int numOfSeats;
    private boolean babiesAllowed;
    private boolean petsAllowed;

    public RegisterVehicleRequest(
            String model,
            String registration,
            String type,
            int numOfSeats,
            boolean babiesAllowed,
            boolean petsAllowed) {

        this.model = model;
        this.registration = registration;
        this.type = type;
        this.numOfSeats = numOfSeats;
        this.babiesAllowed = babiesAllowed;
        this.petsAllowed = petsAllowed;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public int getNumOfSeats() {
        return numOfSeats;
    }

    public void setNumOfSeats(int numOfSeats) {
        this.numOfSeats = numOfSeats;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isBabiesAllowed() {
        return babiesAllowed;
    }

    public void setBabiesAllowed(boolean babiesAllowed) {
        this.babiesAllowed = babiesAllowed;
    }

    public boolean isPetsAllowed() {
        return petsAllowed;
    }

    public void setPetsAllowed(boolean petsAllowed) {
        this.petsAllowed = petsAllowed;
    }
}