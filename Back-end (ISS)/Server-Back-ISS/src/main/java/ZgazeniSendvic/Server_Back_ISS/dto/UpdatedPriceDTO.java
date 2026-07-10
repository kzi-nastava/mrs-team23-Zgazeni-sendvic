package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;
import lombok.Getter;
import lombok.Setter;

public class UpdatedPriceDTO {

    @Getter
    @Setter
    private VehicleType vehicleType;

    @Getter
    @Setter
    private double price;

    @Getter
    @Setter
    private int updatedRidesCount;

    public UpdatedPriceDTO(VehicleType vehicleType, double price, int updatedRidesCount) {
        this.vehicleType = vehicleType;
        this.price = price;
        this.updatedRidesCount = updatedRidesCount;
    }
}
