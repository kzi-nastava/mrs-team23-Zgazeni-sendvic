package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.service.VehicleTrackerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import ZgazeniSendvic.Server_Back_ISS.dto.VehicleTrackerDTO;

@RestController
@RequestMapping("/api/ride-tracking-user")
public class RideTrackingUserController {
    public RideTrackingUserController() {}

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehicleTrackerDTO> rideTracking( @PathVariable("id") Long id) {
        VehicleTrackerService vehicleTrackerService = new VehicleTrackerService();
        VehicleTrackerDTO vehicleTrackerDTO = vehicleTrackerService.getVehicleTrackingData(id);
        if (vehicleTrackerDTO == null) {
            return new ResponseEntity<VehicleTrackerDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<VehicleTrackerDTO>(vehicleTrackerDTO, HttpStatus.OK);
    }

}
