package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;
import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;
import lombok.Getter;
import lombok.Setter;

public class CreatedDriverDTO {
    @Getter @Setter
    Long id;
    @Getter @Setter
    String email;
    @Getter @Setter
    String name;
    @Getter @Setter
    String lastName;
    @Getter @Setter
    String phoneNumber;
    @Getter @Setter
    boolean active;
    @Getter @Setter
    Long vehicleId;
    @Getter @Setter
    String vehicleModel;
    @Getter @Setter
    String vehicleRegistration;
    @Getter @Setter
    VehicleType vehicleType;

    public CreatedDriverDTO() { super(); }
}
