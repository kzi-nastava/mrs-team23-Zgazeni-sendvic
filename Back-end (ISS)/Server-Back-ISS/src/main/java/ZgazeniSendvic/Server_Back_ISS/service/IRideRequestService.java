package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;

public interface IRideRequestService {

    void create(CreateRideRequestDTO dto, Long creatorId);

}
