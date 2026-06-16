package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.DriverStatusChangedDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RegisterVehicleDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;

public interface IDriverService extends IAccountService {

    Driver registerDriver(CreateDriverDTO dto);

    Vehicle registerVehicle(RegisterVehicleDTO dto);

    /**
     * Toggle the currently authenticated driver active/inactive. Returns a human-readable
     * outcome ("active", "inactive", or "marked for deactivation" while still driving).
     */
    DriverStatusChangedDTO changeAvailableStatus(boolean value);

    void activateDriver(String token, String passwordRaw);

    void deactivateDriverIfRequested(boolean status);

}
