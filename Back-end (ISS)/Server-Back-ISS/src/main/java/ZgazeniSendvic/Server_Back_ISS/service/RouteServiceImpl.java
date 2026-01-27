package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.SaveRouteDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Route;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteServiceImpl implements IRouteService {

    @Autowired
    RouteRepository routeRepository;
    @Autowired
    AccountRepository accountRepository;

    public RouteServiceImpl(RouteRepository routeRepository,
                            AccountRepository accountRepository) {
        this.routeRepository = routeRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Route saveRoute(SaveRouteDTO dto) {

        Account owner = accountRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Route route = new Route(
                dto.getStart(),
                dto.getDestination(),
                dto.getMidPoints(),
                owner
        );

        return routeRepository.save(route);
    }

    @Override
    public List<Route> getFavoriteRoutes(Long ownerId) {
        return routeRepository.findByOwnerId(ownerId);
    }
}

