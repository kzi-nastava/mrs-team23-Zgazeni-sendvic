package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

public class VehiclePositionDTO {

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String vehicleId;

    @Getter
    @Setter
    private Double latitude;

    @Getter
    @Setter
    private Double longitude;

    @Getter
    @Setter
    private String status;

    public VehiclePositionDTO() {
    }

    public VehiclePositionDTO(Long id, String vehicleId, Double latitude, Double longitude, String status) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }
}