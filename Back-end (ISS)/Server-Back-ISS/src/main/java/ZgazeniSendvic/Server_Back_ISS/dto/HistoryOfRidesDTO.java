package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class HistoryOfRidesDTO {
    @Getter @Setter
    private List<PastRideDTO> rides;
    public HistoryOfRidesDTO() {}
    public HistoryOfRidesDTO(List<PastRideDTO> rides) {
        this.rides = rides;
    }
}
