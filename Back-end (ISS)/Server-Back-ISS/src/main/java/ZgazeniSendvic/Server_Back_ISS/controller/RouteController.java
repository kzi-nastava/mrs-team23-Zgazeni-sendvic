package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.RouteDTO;
import ZgazeniSendvic.Server_Back_ISS.service.IRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final IRouteService routeService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/favorites")
    public ResponseEntity<List<RouteDTO>> myFavorites() {
        return ResponseEntity.ok(routeService.getMyFavoriteRoutes());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/favorites/from-ride/{rideId}")
    public ResponseEntity<RouteDTO> favoriteFromRide(@PathVariable Long rideId) {
        RouteDTO dto = routeService.saveRouteFromRide(rideId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/favorites/{routeId}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long routeId) {
        routeService.deleteMyRoute(routeId);
        return ResponseEntity.noContent().build();
    }
}

