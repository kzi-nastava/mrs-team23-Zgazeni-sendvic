package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;
import lombok.Getter;
import lombok.Setter;

public class PastRideDTO {
    @Getter @Setter
    private Long id;
    @Getter @Setter
    private Location origin;
    @Getter @Setter
    private Location destination;
    @Getter @Setter
    private String departureTime;
    @Getter @Setter
    private String arrivalTime;
    @Getter @Setter
    private boolean panic;
    @Getter @Setter
    private String canceled;
    @Getter @Setter
    private double price;

    public PastRideDTO() {}

    public PastRideDTO(Long id, Location origin, Location destination, String departureTime, String arrivalTime, boolean panic, String canceled, double price) {
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
