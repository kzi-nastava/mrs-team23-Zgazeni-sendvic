package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;
import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;

public record VehicleDTO(
        Long id,
        String model,
        String registration,
        VehicleType type,
        int numOfSeats,
        boolean babiesAllowed,
        boolean petsAllowed
) {
    public static VehicleDTO from(Vehicle v) {
        return new VehicleDTO(
                v.getId(),
                v.getModel(),
                v.getRegistration(),
                v.getType(),
                v.getNumOfSeats(),
                v.isBabiesAllowed(),
                v.isPetsAllowed()
        );
    }
}
