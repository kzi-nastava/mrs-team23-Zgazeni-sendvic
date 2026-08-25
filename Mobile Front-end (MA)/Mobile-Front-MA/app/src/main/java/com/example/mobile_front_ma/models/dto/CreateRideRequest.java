package com.example.mobile_front_ma.models.dto;

import com.example.mobile_front_ma.models.LocationRequest;
import com.example.mobile_front_ma.models.VehicleType;

import java.util.List;

public class CreateRideRequest {

    private List<LocationRequest> locations;

    private VehicleType vehicleType;

    private boolean babiesAllowed;
    private boolean petsAllowed;

    private String scheduledTime;

    private List<String> invitedPassengerEmails;

    private double estimatedDistanceKm;

    private double estimatedPrice;

    public CreateRideRequest() {
    }

    public List<LocationRequest> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationRequest> locations) {
        this.locations = locations;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
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

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public List<String> getInvitedPassengerEmails() {
        return invitedPassengerEmails;
    }

    public void setInvitedPassengerEmails(List<String> invitedPassengerEmails) {
        this.invitedPassengerEmails = invitedPassengerEmails;
    }

    public double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public void setEstimatedDistanceKm(double estimatedDistanceKm) {
        this.estimatedDistanceKm = estimatedDistanceKm;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }
}