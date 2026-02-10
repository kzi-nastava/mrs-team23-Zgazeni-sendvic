package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.AHORAccountDetailsDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.ARideDetailsRequestedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.ARideRequestedDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("/api/HOR")
public class HORController {


    @GetMapping(path = "/admin/{targetID}",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ARideRequestedDTO>> adminRetrieveRides
            (@PathVariable Long targetID,
             Pageable pageable,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
            throws Exception{
        // here a service would go over the pageable and request params etc...

        ARideRequestedDTO ride = new ARideRequestedDTO(
                7L,
                Arrays.asList("Stop A", "Stop B", "Stop C"),
                "Start Stop",
                new Date(),
                new Date(System.currentTimeMillis() + 3600000),
                false,
                null,
                29.99,
                false
        );

        List<ARideRequestedDTO> allRides = new ArrayList<>();
        allRides.add(ride);

        return new ResponseEntity<List<ARideRequestedDTO>>(allRides, HttpStatus.OK);

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
