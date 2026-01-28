package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.VehiclePosition;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehiclePositionsRepository extends JpaRepository<VehiclePosition, Long> {
    @NullMarked
    Optional<VehiclePosition> findById(Long id);
}
