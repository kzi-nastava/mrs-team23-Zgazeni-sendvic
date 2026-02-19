package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class FutureRidesDTO {
    @Getter
    @Setter
    private List<NextRideDTO> futureRides;
    public FutureRidesDTO() {
    }
    public FutureRidesDTO(List<NextRideDTO> futureRides) {
        this.futureRides = futureRides;
    }
}
