package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

public class PastRideDTO {
    @Getter @Setter
    private Long id;
    @Getter @Setter
    private String origin;
    @Getter @Setter
    private String destination;
    @Getter @Setter
    private String departureTime;
    @Getter @Setter
    private String arrivalTime;
    @Getter @Setter
    private boolean panic;
    @Getter @Setter
    private String canceled;
    @Getter @Setter
    private String price;

    public PastRideDTO() {}

    public PastRideDTO(Long id, String origin, String destination, String departureTime, String arrivalTime, boolean panic, String canceled, String price) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.panic = panic;
        this.canceled = canceled;
        this.price = price;
    }
}
