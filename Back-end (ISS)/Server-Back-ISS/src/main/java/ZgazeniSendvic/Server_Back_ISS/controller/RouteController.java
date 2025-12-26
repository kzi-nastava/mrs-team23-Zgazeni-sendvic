package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.GetRouteDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetRouteDTO> getRoute(@PathVariable("id") Long id) {
        GetRouteDTO route = new GetRouteDTO();

        route.setId(1L);
        route.setStart(new Location(1L, 50.0, 50.0));
        route.setDestination(new Location(1L, 50.0, 50.0));

        return new ResponseEntity<GetRouteDTO>(route, HttpStatus.OK);
    }
}
