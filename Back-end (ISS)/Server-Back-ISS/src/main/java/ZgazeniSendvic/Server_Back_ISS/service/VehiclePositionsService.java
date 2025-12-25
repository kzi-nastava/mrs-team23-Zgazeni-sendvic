package ZgazeniSendvic.Server_Back_ISS.service;

import org.springframework.stereotype.Service;
import ZgazeniSendvic.Server_Back_ISS.dto.VehiclePositionDTO;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehiclePositionsService {
    public List<VehiclePositionDTO> getAllVehiclePositions() {
        List<VehiclePositionDTO> vehiclePositions = new ArrayList<>();
        vehiclePositions.add(new VehiclePositionDTO(1L, 40.7128, -74.0060,"active"));
        vehiclePositions.add(new VehiclePositionDTO(2L, 34.0522, -118.2437,"inactive"));
        vehiclePositions.add(new VehiclePositionDTO(3L, 51.5074, -0.1278,"active"));
        return vehiclePositions;
    }
}
