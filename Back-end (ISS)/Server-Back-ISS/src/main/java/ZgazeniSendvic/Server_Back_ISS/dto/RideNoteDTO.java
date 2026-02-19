package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RideNoteDTO {
    @Setter
    @Getter
    @NotNull(message = "rideId must be provided")
    private Long rideId;
    @Setter
    @Getter
    @NotBlank(message = "note must not be blank")
    private String note;

    public RideNoteDTO() {
    }

    public RideNoteDTO(Long rideId, String note) {
        this.rideId = rideId;
        this.note = note;
    }
}
