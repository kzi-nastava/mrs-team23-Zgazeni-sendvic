package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.RideRequest;

import java.time.LocalDateTime;

public interface IRideRequestService {

    RideRequest createRideRequest(CreateRideRequestDTO dto);

    void recreateRideRequest(Long rideID, LocalDateTime when);

}
