package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.PanicNotificationDTO;
import ZgazeniSendvic.Server_Back_ISS.service.PanicNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/panic-notifications")
public class PanicNotificationController {

    @Autowired
    private PanicNotificationService panicNotificationService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/resolve/{rideID}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> panicRide(@PathVariable Long rideID) throws Exception{

        PanicNotificationDTO notification = panicNotificationService.resolvePanicById(rideID);


        return new ResponseEntity<PanicNotificationDTO>(notification, HttpStatus.OK);


    }



    // Endpoints will be added here as needed
}

