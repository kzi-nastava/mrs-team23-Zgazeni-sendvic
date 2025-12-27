package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.CreatedRideRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/riderequest")
public class RideRequestController {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedRideRequestDTO> orderRide(CreateRideRequestDTO rrequest) {
        CreatedRideRequestDTO createdRRequest = new CreatedRideRequestDTO();

        createdRRequest.setId(1L);

        return new ResponseEntity<CreatedRideRequestDTO>(createdRRequest, HttpStatus.CREATED);
    }
}
