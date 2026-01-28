package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.SaveRouteDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Route;

import java.util.List;

public interface IRouteService {

    Route saveRoute(SaveRouteDTO dto);
    List<Route> getFavoriteRoutes(Long ownerId);
}

