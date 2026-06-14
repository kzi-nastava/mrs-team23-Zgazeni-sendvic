package ZgazeniSendvic.Server_Back_ISS.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountConfirmationDTO {

    @NotBlank
    private String email;

    @NotBlank
    private String code; // 6-digit activation code emailed to the user

}
