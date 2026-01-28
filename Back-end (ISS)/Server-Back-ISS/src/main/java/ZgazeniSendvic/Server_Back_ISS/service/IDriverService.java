package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RegisterVehicleDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;

public interface IDriverService extends IAccountService {

    Driver registerDriver(CreateDriverDTO dto);

    Vehicle registerVehicle(RegisterVehicleDTO dto);

    void changeAvailableStatus(String email, boolean value);

}
