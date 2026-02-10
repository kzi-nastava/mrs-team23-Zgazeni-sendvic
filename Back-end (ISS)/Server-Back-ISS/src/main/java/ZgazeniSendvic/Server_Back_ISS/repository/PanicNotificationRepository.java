package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.PanicNotification;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}



