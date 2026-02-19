package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

public class RideDriverRatingDTO {
    @Getter @Setter
    private Long userId;
    @Getter @Setter
    private Long rideId;
    @Getter @Setter
    private int driverRating;
    @Getter @Setter
    private int vehicleRating;
    @Getter @Setter
    private String comment;

    public RideDriverRatingDTO() {
    }

    public RideDriverRatingDTO(Long userId, Long rideId, int driverRating, int vehicleRating, String comment) {
        this.userId = userId;
        this.rideId = rideId;
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
    }

}
