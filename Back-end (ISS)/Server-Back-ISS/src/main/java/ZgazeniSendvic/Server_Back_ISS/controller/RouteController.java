package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.GetRouteDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.SaveRouteDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
import ZgazeniSendvic.Server_Back_ISS.model.Route;
import ZgazeniSendvic.Server_Back_ISS.service.IAccountService;
import ZgazeniSendvic.Server_Back_ISS.service.IRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    @Autowired
    IRouteService routeService;
    @Autowired
    IAccountService accountService;

    @PostMapping(
            value = "/favorite/save/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<GetRouteDTO> saveFavoriteRoute(@PathVariable("id") Long id,
                                                         @RequestBody SaveRouteDTO dto) {

        Route saved = routeService.saveRoute(dto);
        saved.setOwner(accountService.findAccount(id));

        GetRouteDTO response = mapToDTO(saved);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(
            value = "/favorites/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Collection<GetRouteDTO>> getFavoriteRoutes(
            @PathVariable("id") Long id) {

        List<Route> routes = routeService.getFavoriteRoutes(id);

        List<GetRouteDTO> result = routes.stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(result);
    }

    private GetRouteDTO mapToDTO(Route route) {
        GetRouteDTO dto = new GetRouteDTO();
        dto.setId(route.getId());
        dto.setStart(route.getStart());
        dto.setDestination(route.getDestination());
        dto.setMidPoints(route.getMidPoints());
        dto.setAccount(route.getOwner());
        return dto;
    }
}
