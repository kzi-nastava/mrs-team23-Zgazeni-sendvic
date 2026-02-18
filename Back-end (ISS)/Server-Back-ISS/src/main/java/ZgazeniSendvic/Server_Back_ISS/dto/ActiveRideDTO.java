package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;
import lombok.Getter;
import lombok.Setter;

public class ActiveRideDTO {
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
    private String status;
    @Getter @Setter
    private double price;
    @Getter @Setter
    private String driverEmail;
    @Getter @Setter
    private String date;

    public ActiveRideDTO() {}

    public ActiveRideDTO(Long id, Location origin, Location destination, String departureTime, String arrivalTime, boolean panic, String status, double price, String driverEmail, String date) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.panic = panic;
        this.status = status;
        this.price = price;
        this.driverEmail = driverEmail;
        this.date = date;
    }
}

