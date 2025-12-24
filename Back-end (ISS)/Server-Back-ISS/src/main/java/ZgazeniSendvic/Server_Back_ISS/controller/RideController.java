package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.RouteEstimationDTO;
import ZgazeniSendvic.Server_Back_ISS.entity.RideRoute;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
