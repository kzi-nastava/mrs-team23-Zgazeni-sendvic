package ZgazeniSendvic.Server_Back_ISS.controller;


import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.entity.RideRoute;


import ZgazeniSendvic.Server_Back_ISS.entity.RideRoute;
import org.springframework.format.annotation.DateTimeFormat;
// removed import of all dto's, might break

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.awt.print.Pageable;
import java.time.LocalDate;
// removed import of all utils, might break

@RestController
@RequestMapping("/")
class RideController {

    
    @PutMapping(path = "ride-tracking",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriveCancelledDTO> cancelDrive(@RequestBody DriveCancelDTO cancelRequest) throws Exception{

        //process that would decide whether to or not to
        boolean isCancelled = true;
        if(!isCancelled)
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); //bad reason/ too late etc.

        DriveCancelledDTO cancelled = new DriveCancelledDTO();
        cancelled.setReason(cancelRequest.getReason());
        cancelled.setRideID(cancelRequest.getRideID());
        cancelled.setTime(cancelRequest.getTime());
        cancelled.setRequesterID(cancelRequest.getRequesterID());


        cancelled.setCancelled(true);

        return new ResponseEntity<DriveCancelledDTO>(cancelled, HttpStatus.OK);


    }


    @GetMapping(path = "ride-estimation/{arrival}/{destinationsStr}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RouteEstimationDTO>
    estimateRide(@PathVariable String arrival, @PathVariable String destinationsStr)throws Exception{

        List<String> destinations = new ArrayList<>(Arrays.asList(destinationsStr.split(",")));
        RideRoute route = new RideRoute(destinations);
        RouteEstimationDTO estimation = new RouteEstimationDTO(route.getRoutes(), route.getTotalTime());

        

        return new ResponseEntity<RouteEstimationDTO>(estimation, HttpStatus.OK);


    }

    @PutMapping(path = "ride-tracking/stop/{rideID}",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RideStoppedDTO> stopRide(@RequestBody RideStopDTO stopReq, @PathVariable String rideID)
            throws Exception{
        RideStoppedDTO stopped = new RideStoppedDTO();
        stopped.setRideID(rideID);
        //now a service would determine all passed destinations, remove not passed, and add current location as ending
        double newPrice = 45;
        //in reality, it would have access to all so far passed destinations
        List<String> newDests = new ArrayList<String>();
        newDests.add(stopReq.getCurrentLocation());

        stopped.setNewPrice(45);
        stopped.setUpdatedDestinations(newDests);
        //backend info would also be updated

        return new ResponseEntity<RideStoppedDTO>(stopped, HttpStatus.OK);

    }

    @GetMapping(path = "admin-HOR/{targedID}",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ARideRequestedDTO>> adminRetrieveRides
            (@PathVariable String targetID,
            Pageable pageable,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    throws Exception{
        // here a service would go over the pageable and request params etc...

        ARideRequestedDTO ride = new ARideRequestedDTO(
                "asdasd12313",
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

    @GetMapping(path = "admin-HOR-Detailed/{targetID}",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ARideDetailsRequestedDTO> adminRetrieveDetailed(@PathVariable String targetID)
            throws Exception{

        //would find based on id in service

        List<UserDTO> passengers = Arrays.asList(new UserDTO(), new UserDTO());
        UserDTO driver = new UserDTO();
        List<String> reports = Arrays.asList("Passenger was late","Driver was friendly");
        List<Integer> ratings = Arrays.asList(5, 4, 5);
        ARideDetailsRequestedDTO detailed = new ARideDetailsRequestedDTO(passengers,driver,reports,ratings);

        return new ResponseEntity<ARideDetailsRequestedDTO>(detailed, HttpStatus.OK);

    }



}
