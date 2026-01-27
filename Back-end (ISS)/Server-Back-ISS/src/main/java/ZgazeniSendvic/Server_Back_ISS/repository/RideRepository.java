package ZgazeniSendvic.Server_Back_ISS.repository;


import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    Optional<Ride> findById(Long id);

    @Query("""
        SELECT SUM(r.durationMinutes)
        FROM Ride r
        WHERE r.driver.id = :driverId
          AND r.endTime >= :since
    """)
    Integer getWorkedMinutesLast24h(Long driverId, LocalDateTime since);

    List<Ride> findByDriverAndStatus(Driver driver, RideStatus status);
}
