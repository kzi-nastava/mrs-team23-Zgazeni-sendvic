package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PanicNotificationDTO {
    private Long id;
    private Long callerId;
    private String callerName;
    private Long rideId;
    private LocalDateTime createdAt;
    private boolean resolved;

}

