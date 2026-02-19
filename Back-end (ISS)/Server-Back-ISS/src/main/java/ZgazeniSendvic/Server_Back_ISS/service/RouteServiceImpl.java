package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.RouteDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.Route;
import ZgazeniSendvic.Server_Back_ISS.repository.RouteRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository; // if you have it
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements IRouteService {

    @Autowired
    RouteRepository routeRepository;
    @Autowired
    AccountServiceImpl accountService;
    @Autowired
    RideRepository rideRepository; // or RideService if you prefer

    @Override
    public List<RouteDTO> getMyFavoriteRoutes() {
        Account me = accountService.getCurrentAccount();
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");

        return routeRepository.findByOwnerId(me.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public RouteDTO saveRouteFromRide(Long rideId) {
        Account me = accountService.getCurrentAccount();
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

        // allow only the creator (or passenger) to favorite it:
        if (!ride.getCreator().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        List<Location> locs = ride.getLocations();
        if (locs == null || locs.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride has invalid locations");
        }

        Location start = locs.get(0);
        Location dest = locs.get(locs.size() - 1);
        List<Location> mid = (locs.size() > 2) ? new ArrayList<>(locs.subList(1, locs.size() - 1)) : new ArrayList<>();

        Route route = new Route(start, dest, mid, me);
        Route saved = routeRepository.save(route);

        return toDto(saved);
    }

    @Override
    public void deleteMyRoute(Long routeId) {
        Account me = accountService.getCurrentAccount();
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");

        Route route = routeRepository.findByIdAndOwnerId(routeId, me.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));

        routeRepository.delete(route);
    }

    private RouteDTO toDto(Route r) {
        RouteDTO dto = new RouteDTO();
        dto.id = r.getId();
        dto.start = r.getStart();
        dto.destination = r.getDestination();
        dto.midPoints = r.getMidPoints();
        return dto;
    }
}


