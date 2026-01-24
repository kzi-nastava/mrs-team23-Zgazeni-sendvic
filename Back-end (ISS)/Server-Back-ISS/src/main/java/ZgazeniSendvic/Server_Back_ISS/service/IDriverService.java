package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;

public interface IDriverService extends IAccountService {

    Driver registerDriver(CreateDriverDTO dto);
}
