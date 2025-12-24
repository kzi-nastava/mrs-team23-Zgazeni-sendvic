package ZgazeniSendvic.Server_Back_ISS.controllers;

import ZgazeniSendvic.Server_Back_ISS.entity.RideRoute;
import org.springframework.http.HttpStatus;
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
    @GetMapping("ride-estimation/{arrival}/{destinationsStr}")
    public ResponseEntity<RideRoute> estimateRide(@PathVariable String arrival, @PathVariable String destinationsStr){
        List<String> destinations = new ArrayList<>(Arrays.asList(destinationsStr.split(",")));
        RideRoute route = new RideRoute(destinations);

        

        return new ResponseEntity<RideRoute>(route, HttpStatus.OK);


    }
}
