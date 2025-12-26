package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.UpdateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UpdatedRideRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/route")
public class RideRequestController {

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdatedRideRequestDTO> activateRide(@RequestBody UpdateRideRequestDTO ride,
                                                              @PathVariable Long id) throws Exception {
        UpdatedRideRequestDTO activatedRide = new UpdatedRideRequestDTO();

        activatedRide.setId(1L);
        activatedRide.setStart(ride.getStart());
        activatedRide.setDestination(ride.getDestination());

        return new ResponseEntity<UpdatedRideRequestDTO>(activatedRide, HttpStatus.OK);
    }
}
