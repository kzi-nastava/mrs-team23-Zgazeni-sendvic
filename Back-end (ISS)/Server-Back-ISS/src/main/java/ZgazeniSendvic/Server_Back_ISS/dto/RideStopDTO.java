package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.Date;

public class RideStopDTO {


    private String currentLocation;
    private Date currentTime;

        RideStopDTO(){}
    public RideStopDTO(String currentLocation, Date currentTime) {

        this.currentLocation = currentLocation;
        this.currentTime = currentTime;
    }


    public String getCurrentLocation() {
        return currentLocation;
    }

    public Date getCurrentTime() {
        return currentTime;
    }


    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }
}
