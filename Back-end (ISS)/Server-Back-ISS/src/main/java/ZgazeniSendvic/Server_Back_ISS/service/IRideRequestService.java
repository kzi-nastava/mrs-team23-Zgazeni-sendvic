package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.RideRequest;

public interface IRideRequestService {

    RideRequest createRideRequest(CreateRideRequestDTO dto);

}
