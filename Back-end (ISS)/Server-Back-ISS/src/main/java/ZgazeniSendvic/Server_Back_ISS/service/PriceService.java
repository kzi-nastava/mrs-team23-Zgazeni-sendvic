package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.UpdatedPriceDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.UpdatePriceDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
import ZgazeniSendvic.Server_Back_ISS.model.Price;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
import ZgazeniSendvic.Server_Back_ISS.repository.PriceRepository;
import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class PriceService {

    private static final double PRICE_PER_KM = 120;

    @Autowired
    private PriceRepository priceRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private DistanceCalculator distanceCalculator;

    public double calculatePrice(VehicleType type, double km) {
        double basePrice = priceRepository.findByVehicleType(type)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Price is not configured for vehicle type: " + type
                ))
                .getPrice();

        return basePrice + km * PRICE_PER_KM;
    }

    @Transactional
    public UpdatedPriceDTO updatePrice(UpdatePriceDTO request) {
        if (request == null || request.getVehicleType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "vehicleType must be provided");
        }
        if (request.getPrice() == null) {
            Price existingPrice = priceRepository.findByVehicleType(request.getVehicleType())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Price is not configured for vehicle type: " + request.getVehicleType()
                    ));
            return new UpdatedPriceDTO(request.getVehicleType(), existingPrice.getPrice(), 0);
        }
        if (request.getPrice() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price cannot be negative");
        }

        Price price = priceRepository.findByVehicleType(request.getVehicleType())
                .orElseGet(() -> new Price(request.getVehicleType(), request.getPrice()));
        price.setPrice(request.getPrice());
        priceRepository.save(price);

        List<Ride> ridesToUpdate = rideRepository.findByStatusInAndDriverVehicleType(
                List.of(RideStatus.SCHEDULED),
                request.getVehicleType()
        );

        for (Ride ride : ridesToUpdate) {
            double recalculatedPrice = calculatePrice(request.getVehicleType(), calculateRideDistanceKm(ride));
            ride.setTotalPrice(Math.round(recalculatedPrice));
        }
        rideRepository.saveAll(ridesToUpdate);

        return new UpdatedPriceDTO(request.getVehicleType(), request.getPrice(), ridesToUpdate.size());
    }

    private double calculateRideDistanceKm(Ride ride) {
        List<Location> locations = ride.getLocations();
        if (locations == null || locations.size() < 2) {
            return 0;
        }

        double distance = 0;
        for (int i = 1; i < locations.size(); i++) {
            distance += distanceCalculator.calculateDistanceKm(locations.get(i - 1), locations.get(i));
        }
        return distance;
    }
}
