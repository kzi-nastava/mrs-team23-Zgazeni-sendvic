package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.List;

public class ARideDetailsRequestedDTO {

    private Long rideID;

    public ARideDetailsRequestedDTO(Long rideID, List<UserDTO> passengers,
                                    UserDTO driver, List<String> reports, List<Integer> ratings) {
        this.rideID = rideID;
        this.passengers = passengers;
        this.driver = driver;
        this.reports = reports;
        this.ratings = ratings;
    }

    public Long getRideID() {
        return rideID;
    }

    public void setRideID(Long rideID) {
        this.rideID = rideID;
    }

    private List<UserDTO> passengers;
    private UserDTO driver;
    private List<String> reports;
    private List<Integer> ratings;

    public ARideDetailsRequestedDTO() {
    }

    public ARideDetailsRequestedDTO(List<UserDTO> passengers, UserDTO driver,
                                    List<String> reports, List<Integer> ratings) {
        this.passengers = passengers;
        this.driver = driver;
        this.reports = reports;
        this.ratings = ratings;
    }

    public List<UserDTO> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<UserDTO> passengers) {
        this.passengers = passengers;
    }

    public UserDTO getDriver() {
        return driver;
    }

    public void setDriver(UserDTO driver) {
        this.driver = driver;
    }

    public List<String> getReports() {
        return reports;
    }

    public void setReports(List<String> reports) {
        this.reports = reports;
    }

    public List<Integer> getRatings() {
        return ratings;
    }

    public void setRatings(List<Integer> ratings) {
        this.ratings = ratings;
    }
}
