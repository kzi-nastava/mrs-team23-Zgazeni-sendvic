package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByOwnerId(Long ownerId);
    Optional<Route> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByOwnerIdAndStartLatitudeAndStartLongitudeAndDestinationLatitudeAndDestinationLongitude(
            Long ownerId, double startLat, double startLng, double destLat, double destLng
    );
    @Query("""
        select r
        from Route r
        where r.owner.id = :ownerId
          and (:hasMidpoints is null or
               (:hasMidpoints = true and size(r.midPoints) > 0) or
               (:hasMidpoints = false and size(r.midPoints) = 0))
          and (:minLat is null or r.start.latitude between :minLat and :maxLat)
          and (:minLng is null or r.start.longitude between :minLng and :maxLng)
          and (:dMinLat is null or r.destination.latitude between :dMinLat and :dMaxLat)
          and (:dMinLng is null or r.destination.longitude between :dMinLng and :dMaxLng)
        """)
    Page<Route> findMineFiltered(
            @Param("ownerId") Long ownerId,
            @Param("hasMidpoints") Boolean hasMidpoints,

            @Param("minLat") Double startMinLat,
            @Param("maxLat") Double startMaxLat,
            @Param("minLng") Double startMinLng,
            @Param("maxLng") Double startMaxLng,

            @Param("dMinLat") Double destMinLat,
            @Param("dMaxLat") Double destMaxLat,
            @Param("dMinLng") Double destMinLng,
            @Param("dMaxLng") Double destMaxLng,

            Pageable pageable
    );
}


