package ZgazeniSendvic.Server_Back_ISS.entity;

import java.util.Date;
import java.util.List;

public class Ride {

    private Long rideID;
    private Long driverID;
    private List<String> passengerIDs;
    private String vehicleID;
    private List<String> destinations;
    private Date beginning;
    private Date ending;
    private double price;

    public Ride(){}
    public Ride(Long rideID, Long driverID, List<String> passengerIDs,
                String vehicleID, List<String> destinations, Date beginning, Date ending, double price) {
        this.rideID = rideID;
        this.driverID = driverID;
        this.passengerIDs = passengerIDs;
        this.vehicleID = vehicleID;
        this.destinations = destinations;
        this.beginning = beginning;
        this.ending = ending;
        this.price = price;
    }


    public Long getRideID() {
        return rideID;
    }

    public Long getDriverID() {
        return driverID;
    }

    public List<String> getPassengerIDs() {
        return passengerIDs;
    }

    public String getVehicleID() {
        return vehicleID;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public Date getBeginning() {
        return beginning;
    }

    public Date getEnding() {
        return ending;
    }

    public double getPrice() {
        return price;
    }

    public void setRideID(Long rideID) {
        this.rideID = rideID;
    }

    public void setDriverID(Long driverID) {
        this.driverID = driverID;
    }

    public void setPassengerIDs(List<String> passengerIDs) {
        this.passengerIDs = passengerIDs;
    }

    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }

    public void setDestinations(List<String> destinations) {
        this.destinations = destinations;
    }

    public void setBeginning(Date beginning) {
        this.beginning = beginning;
    }

    public void setEnding(Date ending) {
        this.ending = ending;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
