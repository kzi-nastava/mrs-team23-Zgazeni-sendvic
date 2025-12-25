package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.ARideRequestedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RideStopDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RideStoppedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RouteEstimationDTO;
import ZgazeniSendvic.Server_Back_ISS.entity.RideRoute;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/")
class RideController {
    @GetMapping(path = "ride-estimation/{arrival}/{destinationsStr}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RouteEstimationDTO>
    estimateRide(@PathVariable String arrival, @PathVariable String destinationsStr)throws Exception{

        List<String> destinations = new ArrayList<>(Arrays.asList(destinationsStr.split(",")));
        RideRoute route = new RideRoute(destinations);
        RouteEstimationDTO estimation = new RouteEstimationDTO(route.getRoutes(), route.getTotalTime());

        

        return new ResponseEntity<RouteEstimationDTO>(estimation, HttpStatus.OK);


    }

    @PutMapping(path = "ride-tracking/stop",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RideStoppedDTO> stopRide(@RequestBody RideStopDTO stopReq)throws Exception{
        RideStoppedDTO stopped = new RideStoppedDTO();
        stopped.setRideID(stopReq.getRideID());
        stopped.setDriverID((stopReq.getDriverID()));
        //now a service would determine all passsed destinations, remove not passed, and add current location as ending
        double newPrice = 45;
        //in reality it would have access to all so far passed dests
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
                UUID.randomUUID().toString(),                   //rideID
                Arrays.asList("Stop A", "Stop B", "Stop C"),   //destinations
                "Start Stop",
                new Date(),                                     //beginning
                new Date(System.currentTimeMillis() + 3600000),//ending (1 hour later)
                false,                                          //wasCancelled
                null,                                           //whoCancelled
                29.99,                                          //price
                false                                           //panic
        );

        List<ARideRequestedDTO> allRides = new ArrayList<>();
        allRides.add(ride);

        return new ResponseEntity<List<ARideRequestedDTO>>(allRides, HttpStatus.OK);

    }




}
