package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

public class RideEndDTO {
    @Getter @Setter
    private Long rideId;
    @Getter @Setter
    private Double price;
    @Getter @Setter
    private boolean paid;
    @Getter @Setter
    private boolean ended;

    public RideEndDTO() {
    }

    public RideEndDTO(Long rideId, Double price) {
        this.rideId = rideId;
        this.price = price;
    }

    public RideEndDTO(Long rideId, Double price, boolean paid, boolean ended) {
        this.rideId = rideId;
        this.price = price;
        this.paid = paid;
        this.ended = ended;
    }
}
