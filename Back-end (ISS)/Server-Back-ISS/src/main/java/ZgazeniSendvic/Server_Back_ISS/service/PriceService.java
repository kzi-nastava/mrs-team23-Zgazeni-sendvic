package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;
import org.springframework.stereotype.Service;

@Service
public class PriceService {

    public double calculatePrice(VehicleType type, double km) {
        return type.getBasePrice() + km * 120;
    }
}

