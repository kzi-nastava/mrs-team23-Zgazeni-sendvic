package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.RideEndDTO;
import org.jspecify.annotations.NonNull;

public class RideEndService {
    public boolean RideEndService(RideEndDTO rideEndDTO) {
        System.out.println("Ride successfully ended for ride ID: " + rideEndDTO.getRideId() + ", with price: " + rideEndDTO.getPrice());
        return true;
    }
}
