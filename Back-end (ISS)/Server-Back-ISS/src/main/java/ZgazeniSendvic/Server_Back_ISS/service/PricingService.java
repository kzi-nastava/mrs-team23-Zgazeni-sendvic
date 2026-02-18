package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    public double calculatePrice(VehicleType type, double kilometers) {

        double basePrice = switch (type) {
            case STANDARD -> 300;
            case VAN -> 500;
            case LUXURY -> 800;
        };

        return basePrice + kilometers * 120;
    }
}
