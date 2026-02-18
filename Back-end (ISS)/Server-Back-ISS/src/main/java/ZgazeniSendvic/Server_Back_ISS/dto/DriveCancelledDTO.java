package ZgazeniSendvic.Server_Back_ISS.dto;

//I will kep requester/rideIDs as any requester can cancel any ride, and they should all be aware, so its N-N

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriveCancelledDTO {

    private Long rideID;
    private String reason;
    private LocalDateTime time;
    private boolean isCancelled;

}
