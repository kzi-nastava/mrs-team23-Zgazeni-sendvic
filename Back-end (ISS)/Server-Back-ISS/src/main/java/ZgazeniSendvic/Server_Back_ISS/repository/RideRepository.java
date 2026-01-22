package ZgazeniSendvic.Server_Back_ISS.repository;


import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long> {

    Optional<Ride> findById(Long id);


}
