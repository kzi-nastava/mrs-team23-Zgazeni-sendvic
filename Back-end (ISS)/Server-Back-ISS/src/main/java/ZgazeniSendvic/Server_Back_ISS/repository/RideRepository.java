package ZgazeniSendvic.Server_Back_ISS.repository;


import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

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




    //for HOR

    @Query("""
    SELECT DISTINCT r FROM Ride r
    LEFT JOIN r.passengers p
    WHERE r.driver = :account
       OR r.creator = :account
       OR p = :account
    """)
    Page<Ride> findByAccount(@Param("account") Account account, Pageable pageable);

    @Query("""
    SELECT r FROM Ride r 
    LEFT JOIN r.passengers p 
    WHERE (r.driver = :account OR r.creator = :account OR p = :account)
      AND (CAST(:fromDate AS timestamp) IS NULL OR r.creationDate >= :fromDate)
      AND (CAST(:toDate AS timestamp) IS NULL OR r.creationDate <= :toDate)
    """)
    Page<Ride> findByAccountAndDateRange(
            @Param("account") Account account,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

}
