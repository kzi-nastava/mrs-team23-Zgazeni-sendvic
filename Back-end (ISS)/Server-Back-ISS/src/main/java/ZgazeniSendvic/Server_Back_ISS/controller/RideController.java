package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.DriveCancelDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.DriveCancelledDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RouteEstimationDTO;
import ZgazeniSendvic.Server_Back_ISS.entity.RideRoute;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/")
class RideController {
    
    @PutMapping(path = "ride-tracking",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriveCancelledDTO> cancelDrive(@RequestBody DriveCancelDTO cancelRequest) throws Exception{

        DriveCancelledDTO cancelled = new DriveCancelledDTO();
        cancelled.setReason(cancelRequest.getReason());
        cancelled.setRideID(cancelRequest.getRideID());
        cancelled.setTime(cancelRequest.getTime());
        cancelled.setRequesterID(cancelRequest.getRequesterID());

        //process that would decide wether to or not to
        cancelled.setCancelled(true);

        return new ResponseEntity<DriveCancelledDTO>(cancelled, HttpStatus.OK);


    }

}
