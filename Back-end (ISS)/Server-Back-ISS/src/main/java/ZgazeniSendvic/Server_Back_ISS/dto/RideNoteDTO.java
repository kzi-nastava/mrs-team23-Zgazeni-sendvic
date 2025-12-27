package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;

public class RideNoteDTO {
    @Setter
    @Getter
    private Long rideId;
    @Setter
    @Getter
    private String note;

    public RideNoteDTO() {
    }

    public RideNoteDTO(Long rideId, String note) {
        this.rideId = rideId;
        this.note = note;
    }
}
