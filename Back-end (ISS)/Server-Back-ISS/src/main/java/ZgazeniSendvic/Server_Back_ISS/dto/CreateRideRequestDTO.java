package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;
import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

public class CreateRideRequestDTO {

    /**
     * Ordered list of locations:
     * index 0 = start
     * last index = destination
     * everything in between = mid-points (order matters)
     */
    private List<Location> locations;

    /**
     * Vehicle constraints
     */
    private VehicleType vehicleType;
    private boolean babiesAllowed;
    private boolean petsAllowed;

    /**
     * Optional scheduled ride
     * null = immediate ride
     */
    private LocalDateTime scheduledTime;

    /**
     * Passengers invited by email
     */
    private List<String> invitedPassengerEmails;

    /**
     * Estimated values calculated on frontend (or via map service)
     */
    private double estimatedDistanceKm;

    /* ---------- GETTERS / SETTERS ---------- */

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
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

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
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
}
