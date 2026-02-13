package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.RideDriverRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RideDriverRatingRepository extends JpaRepository<RideDriverRating, Long> {

    List<RideDriverRating> findByRideId(Long rideId);

}
