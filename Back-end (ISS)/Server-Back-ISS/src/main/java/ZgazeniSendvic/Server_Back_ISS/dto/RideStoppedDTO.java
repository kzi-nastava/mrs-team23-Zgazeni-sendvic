package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.List;

public class RideStoppedDTO {

    private Long rideID;
    private double newPrice;
    private List<String> updatedDestinations;

    public RideStoppedDTO(){}
    public RideStoppedDTO(Long rideID, double newPrice, List<String> updatedDestinations) {
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

    public List<String> getUpdatedDestinations() {
        return updatedDestinations;
    }


    public void setRideID(Long rideID) {
        this.rideID = rideID;
    }

    public void setNewPrice(double newPrice) {
        this.newPrice = newPrice;
    }

    public void setUpdatedDestinations(List<String> updatedDestinations) {
        this.updatedDestinations = updatedDestinations;
    }
}
