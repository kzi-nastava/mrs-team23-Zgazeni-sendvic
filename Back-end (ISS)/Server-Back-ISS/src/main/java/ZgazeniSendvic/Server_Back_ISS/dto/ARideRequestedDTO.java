package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.Date;
import java.util.List;

public class ARideRequestedDTO {
    private String rideID;
    private List<String> destinations;
    private String arrivingPoint;
    private Date beginning;
    private Date ending;
    private boolean wasCancelled;
    private String whoCancelled;
    private double price;
    private boolean panic;

    public ARideRequestedDTO(){};

    public ARideRequestedDTO(String rideID, List<String> destinations, String arrivingPoint,
                             Date beginning, Date ending, boolean wasCancelled, String whoCancelled,
                             double price, boolean panic) {
        this.rideID = rideID;
        this.destinations = destinations;
        this.arrivingPoint = arrivingPoint;
        this.beginning = beginning;
        this.ending = ending;
        this.wasCancelled = wasCancelled;
        this.whoCancelled = whoCancelled;
        this.price = price;
        this.panic = panic;
    }

    public String getRideID() {
        return rideID;
    }

    public void setRideID(String rideID) {
        this.rideID = rideID;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<String> destinations) {
        this.destinations = destinations;
    }

    public String getArrivingPoint() {
        return arrivingPoint;
    }

    public void setArrivingPoint(String arrivingPoint) {
        this.arrivingPoint = arrivingPoint;
    }

    public Date getBeginning() {
        return beginning;
    }

    public void setBeginning(Date beginning) {
        this.beginning = beginning;
    }

    public Date getEnding() {
        return ending;
    }

    public void setEnding(Date ending) {
        this.ending = ending;
    }

    public boolean isWasCancelled() {
        return wasCancelled;
    }

    public void setWasCancelled(boolean wasCancelled) {
        this.wasCancelled = wasCancelled;
    }

    public String getWhoCancelled() {
        return whoCancelled;
    }

    public void setWhoCancelled(String whoCancelled) {
        this.whoCancelled = whoCancelled;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isPanic() {
        return panic;
    }

    public void setPanic(boolean panic) {
        this.panic = panic;
    }
}
