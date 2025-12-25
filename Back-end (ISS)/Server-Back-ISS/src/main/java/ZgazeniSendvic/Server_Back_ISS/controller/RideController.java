package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.RideStopDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RideStoppedDTO;
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



}
