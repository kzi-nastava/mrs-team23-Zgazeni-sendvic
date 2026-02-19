package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.RouteDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Route;

import java.util.List;

public interface IRouteService {
    List<RouteDTO> getMyFavoriteRoutes();
    RouteDTO saveRouteFromRide(Long rideId);
    void deleteMyRoute(Long routeId);
}


