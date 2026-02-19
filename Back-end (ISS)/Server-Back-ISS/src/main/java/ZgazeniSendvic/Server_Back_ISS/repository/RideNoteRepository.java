package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.RideNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideNoteRepository extends JpaRepository<RideNote, Long> {

    List<RideNote> findByRideId(Long rideId);

}
