package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

public class NextRideDTO {
    @Getter @Setter
    private Long rideId;
    @Getter @Setter
    private String startLocation;
    @Getter @Setter
    private String endLocation;
    @Getter @Setter
    private String departureTime;

    public NextRideDTO() {
    }

    public NextRideDTO(Long rideId, String startLocation, String endLocation, String departureTime) {
        this.rideId = rideId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.departureTime = departureTime;
    }
}
