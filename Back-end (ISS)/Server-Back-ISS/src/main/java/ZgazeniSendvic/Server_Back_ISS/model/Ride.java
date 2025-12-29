package ZgazeniSendvic.Server_Back_ISS.model;

import lombok.Getter;
import lombok.Setter;

public class Ride {
    @Getter @Setter
    private Long id;
    @Getter @Setter
    private String origin;
    @Getter @Setter
    private String destination;
    @Getter @Setter
    private String departureTime;
    @Getter @Setter
    private String timeLeft;
    @Getter @Setter
    private Double latitude;
    @Getter @Setter
    private Double longitude;
    @Getter @Setter
    private boolean panic;
    @Getter @Setter
    private String canceled;
    @Getter @Setter
    private String price;

    public Ride() {}

    public Ride(Long id, String origin, String destination, String departureTime, String timeLeft, Double latitude, Double longitude, boolean panic, String canceled, String price) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.timeLeft = timeLeft;
        this.latitude = latitude;
        this.longitude = longitude;
        this.panic = panic;
        this.canceled = canceled;
        this.price = price;
    }
}
