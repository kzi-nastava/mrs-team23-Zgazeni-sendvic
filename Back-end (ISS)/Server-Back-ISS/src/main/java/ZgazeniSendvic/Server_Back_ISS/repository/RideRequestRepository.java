package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.RequestStatus;
import ZgazeniSendvic.Server_Back_ISS.model.RideRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, Long> {

    List<RideRequest> findByStatus(RequestStatus status);
    @Query("""
        select distinct rr
        from RideRequest rr
        join rr.creator c
        left join fetch rr.locations
        where c.email = :email
        order by rr.id desc
    """)
    List<RideRequest> findTop10ByCreator_EmailOrderByIdDesc(String email);
    RideRequest save(CreateRideRequestDTO dto);

}

