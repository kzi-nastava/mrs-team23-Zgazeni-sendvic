package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;

import java.util.List;

public class RideStoppedDTO {

    private Long rideID;
    private double newPrice;
    private List<Location> updatedDestinations;

    public RideStoppedDTO(){}
    public RideStoppedDTO(Long rideID, double newPrice, List<Location> updatedDestinations) {
        this.rideID = rideID;
        this.newPrice = newPrice;
        this.updatedDestinations = updatedDestinations;
    }



    public Long getRideID() {
        return rideID;
    }

    public double getNewPrice() {
        return newPrice;
    }

    public List<Location> getUpdatedDestinations() {
        return updatedDestinations;
    }


    public void setRideID(Long rideID) {
        this.rideID = rideID;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }

    public void setUpdatedDestinations(List<Location> updatedDestinations) {
        this.updatedDestinations = updatedDestinations;
    }
}
