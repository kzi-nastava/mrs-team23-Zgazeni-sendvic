package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

public class VehiclePositionDTO{
    @Getter @Setter
    private Long id;
    @Getter @Setter
    private Double latitude;
    @Getter @Setter
    private Double longitude;
    @Getter @Setter
    private String status;

    public VehiclePositionDTO() {
    }

    public VehiclePositionDTO(Long id, Double latitude, Double longitude,String status) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }
}