package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class RideStopDTO {


    private List<Location> passedLocations;
    private LocalDateTime currentTime;

        RideStopDTO(){}
    public RideStopDTO(List<Location> passedLocations, LocalDateTime currentTime) {

        this.passedLocations = passedLocations;
        this.currentTime = currentTime;
    }


    public List<Location> getPassedLocations() {
        return passedLocations;
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }


    public void setPassedLocations(List<Location> currentLocation) {
        this.passedLocations = currentLocation;
    }

    public void setCurrentTime(LocalDateTime currentTime) {
        this.currentTime = currentTime;
    }
}
