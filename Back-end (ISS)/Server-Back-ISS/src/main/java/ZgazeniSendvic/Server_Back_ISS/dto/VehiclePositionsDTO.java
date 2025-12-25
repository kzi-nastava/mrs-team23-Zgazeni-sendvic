package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.List;

public class VehiclePositionsDTO {
    private List<VehiclePositionDTO> vehiclePositions;

    public VehiclePositionsDTO() {
    }

    public VehiclePositionsDTO(List<VehiclePositionDTO> vehiclePositions) {
        this.vehiclePositions = vehiclePositions;
    }

    public List<VehiclePositionDTO> getVehiclePositions() {
        return vehiclePositions;
    }

    public void setVehiclePositions(List<VehiclePositionDTO> vehiclePositions) {
        this.vehiclePositions = vehiclePositions;
    }
}