package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.ARideRequestedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.PanicNotificationDTO;
import ZgazeniSendvic.Server_Back_ISS.service.PanicNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/retrieve-all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<PanicNotificationDTO>> adminRetrieveRides
            (
             @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime fromDate,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime toDate)
            throws Exception{

        Page<PanicNotificationDTO> allPanics = panicNotificationService.getAllPanicNotifications(pageable, fromDate, toDate);


        return new ResponseEntity<Page<PanicNotificationDTO>>(allPanics, HttpStatus.OK);

    }



    // Endpoints will be added here as needed
}

