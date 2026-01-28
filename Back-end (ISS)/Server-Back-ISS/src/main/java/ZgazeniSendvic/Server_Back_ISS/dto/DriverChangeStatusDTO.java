package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DriverChangeStatusDTO {
    String token;
    String email; //temp so I dont have to turn on token stuff constantly
    boolean toState;
}
