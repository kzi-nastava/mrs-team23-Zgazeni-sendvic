package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RideRequestServiceImpl implements IRideRequestService {

    @Autowired
    RideRequestRepository rideRequestRepository;
    @Autowired
    AccountRepository accountRepository;

    public RideRequestServiceImpl(
            RideRequestRepository rideRequestRepository,
            AccountRepository accountRepository
    ) {
        this.rideRequestRepository = rideRequestRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public void create(CreateRideRequestDTO dto, Long creatorId) {

        Account creator = accountRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        RideRequest request = new RideRequest();

        request.setCreator(creator);
        request.setLocations(dto.getLocations());
        request.setVehicleType(dto.getVehicleType());
        request.setBabiesAllowed(dto.isBabiesAllowed());
        request.setPetsAllowed(dto.isPetsAllowed());
        request.setScheduledTime(dto.getScheduledTime());
        request.setEstimatedDistanceKm(dto.getEstimatedDistanceKm());

        // invited passengers by email
        List<Account> invited = new ArrayList<>();
        if (dto.getInvitedPassengerEmails() != null) {
            for (String email : dto.getInvitedPassengerEmails()) {
                accountRepository.findByEmail(email)
                        .ifPresent(invited::add);
            }
        }
        request.setInvitedPassengers(invited);

        // price calculation (as per spec)
        double basePrice = dto.getVehicleType().getBasePrice();
        double price = basePrice + dto.getEstimatedDistanceKm() * 120;
        request.setEstimatedPrice(price);

        request.setStatus(RequestStatus.PENDING);

        rideRequestRepository.save(request);
    }
}

