package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

public class RidesOverviewDTO {
    @Getter @Setter
    private List<ActiveRideDTO> activeRides;

    public RidesOverviewDTO() {}

    public RidesOverviewDTO(List<ActiveRideDTO> activeRides) {
        this.activeRides = activeRides;
    }
}
