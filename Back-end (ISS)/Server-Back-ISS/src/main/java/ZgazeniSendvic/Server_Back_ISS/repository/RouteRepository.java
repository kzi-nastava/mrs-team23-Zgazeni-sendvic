package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByOwnerId(Long ownerId);
    Optional<Route> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByOwnerIdAndStartLatitudeAndStartLongitudeAndDestinationLatitudeAndDestinationLongitude(
            Long ownerId, double startLat, double startLng, double destLat, double destLng
    );
}


