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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    @GetMapping(path = "/favorites/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<GetRouteDTO>> getFavoriteRoutes(@PathVariable("id") Long id) {
        Collection<GetRouteDTO> routes = new ArrayList<>();
        GetRouteDTO route1 = new GetRouteDTO();
        GetRouteDTO route2 = new GetRouteDTO();

        route1.setId(1L);
        route1.setStart(new Location(1L, 50.0, 50.0));
        route1.setDestination(new Location(1L, 50.0, 50.0));

        route2.setId(1L);
        route2.setStart(new Location(1L, 50.0, 50.0));
        route2.setDestination(new Location(1L, 50.0, 50.0));

        routes.add(route1);
        routes.add(route2);

        return new ResponseEntity<Collection<GetRouteDTO>>(routes, HttpStatus.OK);
    }
}
