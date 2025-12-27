package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class VehiclePositionsDTO {
    @Getter @Setter
    private List<VehiclePositionDTO> vehiclePositions;

    public VehiclePositionsDTO() {
    }

    public VehiclePositionsDTO(List<VehiclePositionDTO> vehiclePositions) {
        this.vehiclePositions = vehiclePositions;
    }
}