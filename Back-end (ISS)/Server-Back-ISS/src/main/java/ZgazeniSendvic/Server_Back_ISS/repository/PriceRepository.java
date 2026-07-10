package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.Price;
import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findByVehicleType(VehicleType vehicleType);

    boolean existsByVehicleType(VehicleType vehicleType);
}
