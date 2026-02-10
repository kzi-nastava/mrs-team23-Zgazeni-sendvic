package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.AHORAccountDetailsDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.ARideDetailsRequestedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.ARideRequestedDTO;
import ZgazeniSendvic.Server_Back_ISS.service.HistoryOfRidesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("/api/HOR")
public class HORController {

    @Autowired
    HistoryOfRidesService historyOfRidesService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/admin/{targetID}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<ARideRequestedDTO>> adminRetrieveRides
            (@PathVariable Long targetID,
             @PageableDefault(sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate)
            throws Exception{
        // here a service would go over the pageable and request params etc...



        Page<ARideRequestedDTO> allRides = historyOfRidesService.getAllRidesOfAccount
                (targetID,pageable,startDate,endDate);

        return new ResponseEntity<Page<ARideRequestedDTO>>(allRides, HttpStatus.OK);

    }

    @GetMapping(path = "admin/Detailed/{targetID}",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ARideDetailsRequestedDTO> adminRetrieveDetailed(@PathVariable Long targetID)
            throws Exception{

        //would find based on id in service

        List<AHORAccountDetailsDTO> passengers = Arrays.asList(new AHORAccountDetailsDTO(), new AHORAccountDetailsDTO());
        AHORAccountDetailsDTO driver = new AHORAccountDetailsDTO();
        List<String> reports = Arrays.asList("Passenger was late","DRIVER was friendly");
        List<Integer> ratings = Arrays.asList(5, 4, 5);
        ARideDetailsRequestedDTO detailed = new ARideDetailsRequestedDTO(targetID,passengers,driver,reports,ratings);

        return new ResponseEntity<ARideDetailsRequestedDTO>(detailed, HttpStatus.OK);

    }

}
