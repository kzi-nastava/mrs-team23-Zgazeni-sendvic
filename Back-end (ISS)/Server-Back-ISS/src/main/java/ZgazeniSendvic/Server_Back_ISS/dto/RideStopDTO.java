package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.Date;

public class RideStopDTO {


    private String passedLocations;
    private Date currentTime;

        RideStopDTO(){}
    public RideStopDTO(String passedLocations, Date currentTime) {

        this.passedLocations = passedLocations;
        this.currentTime = currentTime;
    }


    public String getPassedLocations() {
        return passedLocations;
    }

    public Date getCurrentTime() {
        return currentTime;
    }


    public void setPassedLocations(String currentLocation) {
        this.passedLocations = currentLocation;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }
}
