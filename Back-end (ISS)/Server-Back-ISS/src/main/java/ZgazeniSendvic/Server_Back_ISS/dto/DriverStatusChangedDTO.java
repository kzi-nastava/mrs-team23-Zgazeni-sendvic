package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.*;

/**
 * Result of PUT /api/driver/changeStatus: the driver's true availability after the
 * call (so clients reflect actual state, not the requested one), plus a message.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverStatusChangedDTO {
    private boolean available;
    private String message;
}
