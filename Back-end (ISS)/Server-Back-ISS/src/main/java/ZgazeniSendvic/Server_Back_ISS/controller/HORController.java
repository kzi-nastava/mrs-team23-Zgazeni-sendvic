package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.service.HistoryOfRidesService;
import ZgazeniSendvic.Server_Back_ISS.service.IRideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("/api/HOR")
public class HORController {

    @Autowired
    HistoryOfRidesService historyOfRidesService;

    @Autowired
    IRideService rideService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/admin/{targetID}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ARideRequestedDTO>> adminRetrieveRides
            (@PathVariable Long targetID,
             @PageableDefault(sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate)
            throws Exception{
        // here a service would go over the pageable and request params etc...



        Page<ARideRequestedDTO> allRides = historyOfRidesService.getAllRidesOfAccount
                (targetID,pageable,fromDate,toDate);

        return new ResponseEntity<Page<ARideRequestedDTO>>(allRides, HttpStatus.OK);

    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ARideRequestedUserDTO>> userRetrieveRides
            (
             @PageableDefault(sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate)
            throws Exception{
        // here a service would go over the pageable and request params etc...



        Page<ARideRequestedUserDTO> allRides = historyOfRidesService.getAllRidesOfAccountUser
        (pageable,fromDate,toDate);;

        return new ResponseEntity<Page<ARideRequestedUserDTO>>(allRides, HttpStatus.OK);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "admin/detailed/{targetID}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ARideDetailsRequestedDTO> adminRetrieveDetailed(@PathVariable Long targetID)
            throws Exception{

        ARideDetailsRequestedDTO detailed = historyOfRidesService.getRideDetailsForAdmin(targetID);

        return new ResponseEntity<ARideDetailsRequestedDTO>(detailed, HttpStatus.OK);

    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "user/detailed/{targetID}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<URideDetailsRequestedDTO> userRetrieveDetailed(@PathVariable Long targetID)
            throws Exception{

        URideDetailsRequestedDTO detailed = historyOfRidesService.getRideDetailsForUser(targetID);

        return new ResponseEntity<URideDetailsRequestedDTO>(detailed, HttpStatus.OK);

    }

    @PreAuthorize("hasAnyRole('DRIVER','ADMIN','USER')")
    @GetMapping("/report")
    public ResponseEntity<RideReportDTO> report(

            @AuthenticationPrincipal Account account,

            @RequestParam(required = false)
            Long targetUserId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to

    ) {

        return ResponseEntity.ok(
                rideService.generateReport(
                        targetUserId,
                        from,
                        to
                )
        );
    }
}
