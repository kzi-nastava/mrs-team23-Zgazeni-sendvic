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
public class RouteEstimationRequestDTO {
    @NotBlank(message = "Beginning destination cannot be blank")
    private String beginningDestination;
    @NotBlank(message = "Ending destination cannot be blank")
    private String endingDestination;
}
