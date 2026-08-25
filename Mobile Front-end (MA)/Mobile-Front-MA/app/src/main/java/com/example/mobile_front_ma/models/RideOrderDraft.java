package com.example.mobile_front_ma.models;

import java.util.List;

public class RideOrderDraft {

    private final List<Place> locations;

    private final VehicleType vehicleType;

    private final boolean babiesAllowed;
    private final boolean petsAllowed;

    private final List<String> invitedPassengerEmails;

    private final String scheduledTime;

    private final double estimatedDistanceKm;
    private final double estimatedPrice;

    public RideOrderDraft(
            List<Place> locations,
            VehicleType vehicleType,
            boolean babiesAllowed,
            boolean petsAllowed,
            List<String> invitedPassengerEmails,
            String scheduledTime,
            double estimatedDistanceKm,
            double estimatedPrice) {

        this.locations = locations;
        this.vehicleType = vehicleType;
        this.babiesAllowed = babiesAllowed;
        this.petsAllowed = petsAllowed;
        this.invitedPassengerEmails = invitedPassengerEmails;
        this.scheduledTime = scheduledTime;
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.estimatedPrice = estimatedPrice;
    }

    public List<Place> getLocations() {
        return locations;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public boolean isBabiesAllowed() {
        return babiesAllowed;
    }

    public boolean isPetsAllowed() {
        return petsAllowed;
    }

    public List<String> getInvitedPassengerEmails() {
        return invitedPassengerEmails;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }
}