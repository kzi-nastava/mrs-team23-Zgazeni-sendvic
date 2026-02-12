package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HORAccountDetailsDTO {

    private String email;
    private String firstName;
    private String lastName;
    private Long accountId;


}
