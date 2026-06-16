package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DriverChangeStatusDTO {
    // token/email are no longer trusted: the driver is identified from the JWT.
    // They're kept for backwards compatibility with existing clients.
    String token;
    String email;
    boolean toState; // the desired target state: true = active, false = inactive
}
