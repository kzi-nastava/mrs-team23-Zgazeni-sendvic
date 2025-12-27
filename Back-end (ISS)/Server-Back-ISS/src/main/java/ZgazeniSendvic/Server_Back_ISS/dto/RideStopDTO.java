package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.Date;

public class RideStopDTO {

    private String driverID;
    private String rideID;
    private String currentLocation;
    private Date currentTime;

        RideStopDTO(){}
    public RideStopDTO(String driverID, String rideID, String currentLocation, Date currentTime) {
        this.driverID = driverID;
        this.rideID = rideID;
        this.currentLocation = currentLocation;
        this.currentTime = currentTime;
    }


    public String getDriverID() {
        return driverID;
    }

    public String getRideID() {
        return rideID;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public void setRideID(String rideID) {
        this.rideID = rideID;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }
}
