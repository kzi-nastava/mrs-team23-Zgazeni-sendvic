package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.service.IRideRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/riderequest")
public class RideRequestController {

    @Autowired
    IRideRequestService rideRequestService;

    @PostMapping("/create")
    public ResponseEntity<Void> requestRide(
            @RequestBody CreateRideRequestDTO dto) {
        rideRequestService.createRideRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PreAuthorize("hasAnyRole('DRIVER','ADMIN','USER')")
    @PostMapping(path = "ride-reorder/{rideID}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reorderDrive(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime fromDate,
            @PathVariable Long rideID) throws Exception{

        rideRequestService.recreateRideRequest(rideID, fromDate);


        return ResponseEntity.status(HttpStatus.CREATED).build();


    }

}
