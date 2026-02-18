package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.PanicNotification;
import ZgazeniSendvic.Server_Back_ISS.dto.PanicNotificationDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PanicNotificationRepository extends JpaRepository<PanicNotification, Long> {

    Optional<PanicNotification> findById(Long id);


    Optional<PanicNotification> findByRide(Ride ride);

    //Find panic notification by ride ID
    @Query("""
        SELECT pn FROM PanicNotification pn
        WHERE pn.ride.id = :rideId
    """)
    Optional<PanicNotification> findByRideId(@Param("rideId") Long rideId);


    // Find panic notifications by caller (one-to-many relationship)
    List<PanicNotification> findByCaller(Account caller);

    // Find panic notifications by caller ordered by creation time (newest first)
    @Query("""
        SELECT pn FROM PanicNotification pn
        WHERE pn.caller.id = :callerId
        ORDER BY pn.createdAt DESC
    """)
    List<PanicNotification> findByCallerIdOrderByCreatedAtDesc(@Param("callerId") Long callerId);

    // Find all unresolved panic notifications with pagination
    @Query("""
        SELECT pn FROM PanicNotification pn
        WHERE pn.resolved = false
        ORDER BY pn.createdAt DESC
    """)
    Page<PanicNotification> findUnresolvedPanics(Pageable pageable);

    List<PanicNotification> findByResolved(boolean resolved);

    // Find all unresolved panic notifications ordered by creation time (newest first)
    @Query("""
        SELECT pn FROM PanicNotification pn
        WHERE pn.resolved = false
        ORDER BY pn.createdAt DESC
    """)
    List<PanicNotification> findAllUnresolvedOrderByCreatedAtDesc();

    // Find all panic notifications with optional date range filtering
    @Query("""
        SELECT pn FROM PanicNotification pn
        WHERE (CAST(:fromDate AS timestamp) IS NULL OR pn.createdAt >= :fromDate)
          AND (CAST(:toDate AS timestamp) IS NULL OR pn.createdAt <= :toDate)
    """)
    Page<PanicNotification> findAll(Pageable pageable,
                                   @Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate);

    // Find all panic notifications with optional date range filtering as DTOs
    @Query("""
        SELECT new ZgazeniSendvic.Server_Back_ISS.dto.PanicNotificationDTO(
            pn.id,
            pn.caller.id,
            CONCAT(pn.caller.name, ' ', pn.caller.lastName),
            pn.ride.id,
            pn.createdAt,
            pn.resolved,
            pn.resolvedAt
        )
        FROM PanicNotification pn
        WHERE (CAST(:fromDate AS timestamp) IS NULL OR pn.createdAt >= :fromDate)
          AND (CAST(:toDate AS timestamp) IS NULL OR pn.createdAt <= :toDate)
    """)
    Page<PanicNotificationDTO> findAllDtos(Pageable pageable,
                                           @Param("fromDate") LocalDateTime fromDate,
                                           @Param("toDate") LocalDateTime toDate);
}
