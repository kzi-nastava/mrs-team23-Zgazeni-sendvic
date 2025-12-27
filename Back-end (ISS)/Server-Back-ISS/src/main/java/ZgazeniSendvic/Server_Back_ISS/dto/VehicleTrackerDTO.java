package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

public class VehicleTrackerDTO {
    @Setter
    @Getter
    private Long vehicleId;
    @Setter
    @Getter
    private Double latitude;
    @Setter
    @Getter
    private Double longitude;
    @Setter
    @Getter
    private String timeLeft;

    public VehicleTrackerDTO() {
    }

    public VehicleTrackerDTO(Long vehicleId, Double latitude, Double longitude, String timeLeft) {
        this.vehicleId = vehicleId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeLeft = timeLeft;
    }
}
