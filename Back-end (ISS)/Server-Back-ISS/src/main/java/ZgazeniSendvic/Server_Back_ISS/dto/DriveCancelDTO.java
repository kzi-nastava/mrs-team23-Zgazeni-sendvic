package ZgazeniSendvic.Server_Back_ISS.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriveCancelDTO {
    // has to contain info on who sent it/ provided by auth
    private String reason;
    private String rideToken;


}
