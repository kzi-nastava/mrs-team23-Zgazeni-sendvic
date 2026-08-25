package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverRideDTO {

    private Long rideId;
    private String startLocation;
    private String endLocation;
    private String scheduledTime;
    private String status;
    private Double price;
    private int passengerCount;

    public DriverRideDTO() {
    }
}