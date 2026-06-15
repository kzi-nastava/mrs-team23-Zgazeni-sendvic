package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RideRequestServiceImpl implements IRideRequestService {

    @Autowired
    RideRequestRepository rideRequestRepository;
    @Autowired
    RideRepository rideRepository;
    @Autowired
    AccountServiceImpl accountService;
    @Autowired
    PricingService pricingService;
    @Autowired
    DriverAssignmentService driverAssignmentService;

    public RideRequest createRideRequest(CreateRideRequestDTO dto) {

        Account creator = accountService.getCurrentAccount();
        if (creator == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        // basic validation
        if (dto.getLocations() == null || dto.getLocations().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least start and destination are required");
        }

        // 5 hour scheduling rule
        if (dto.getScheduledTime() != null &&
                dto.getScheduledTime().isAfter(LocalDateTime.now().plusHours(5))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ride can only be scheduled up to 5 hours in advance.");
        }

        RideRequest rr = new RideRequest();
        rr.setCreator(creator);
        rr.setLocations(dto.getLocations());
        rr.setVehicleType(dto.getVehicleType());
        rr.setBabiesAllowed(dto.isBabiesAllowed());
        rr.setPetsAllowed(dto.isPetsAllowed());
        rr.setScheduledTime(dto.getScheduledTime());
        List<Account> invited = accountService.resolveAccountsByEmails(
                dto.getInvitedPassengerEmails(),
                creator
        );
        rr.setInvitedPassengers(invited);

        double price = pricingService.calculatePrice(
                dto.getVehicleType(),
                dto.getEstimatedDistanceKm()
        );

        rr.setEstimatedDistanceKm(dto.getEstimatedDistanceKm());
        rr.setEstimatedPrice(price);
        rr.setStatus(RequestStatus.PENDING);

        RideRequest saved = rideRequestRepository.save(rr);

        driverAssignmentService.tryAssignDriver(saved.getId());

        return saved;
    }


    @Transactional
    public void recreateRideRequest(Long rideID, LocalDateTime when){
        // The id comes from the ride history, which lists Rides (not RideRequests). Those are
        // two different tables with independent id sequences, so we must look up the Ride here
        // and rebuild a fresh request from it (spec 2.9.1 / 2.9.3 "order the same route again").
        Ride ride = rideRepository.findById(rideID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));

        // The Ride doesn't store the original vehicle type / distance, so we fall back to a
        // sensible default and a 0 distance. Only the route (locations) is preserved.
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(0);
        dto.setLocations(new ArrayList<>(ride.getLocations()));
        dto.setInvitedPassengerEmails(new ArrayList<>());
        dto.setScheduledTime(when);
        createRideRequest(dto);
    }


}

