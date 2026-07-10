package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;
import lombok.Getter;
import lombok.Setter;

public class UpdatePriceDTO {

    @Getter
    @Setter
    private VehicleType vehicleType;

    @Getter
    @Setter
    private Double price;
}
