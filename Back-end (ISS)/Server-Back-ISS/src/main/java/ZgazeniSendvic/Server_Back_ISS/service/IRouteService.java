package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.RouteDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRouteService {
    List<RouteDTO> getMyFavoriteRoutes();
    RouteDTO saveRouteFromRide(Long rideId);
    void deleteMyRoute(Long routeId);
    Page<RouteDTO> getMyFavoriteRoutesPaged(Boolean hasMidpoints, Double startMinLat, Double startMaxLat,
                                            Double startMinLng, Double startMaxLng, Double destMinLat,
                                            Double destMaxLat, Double destMinLng, Double destMaxLng, Pageable pageable);
}


