package ZgazeniSendvic.Server_Back_ISS.websocket;

import ZgazeniSendvic.Server_Back_ISS.dto.RideTrackingUpdateDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RideTrackingWebSocketService {

    private final RideRepository rideRepository;
    private final AccountRepository accountRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 5000)
    @Transactional(readOnly = true)
    public void sendRideTrackingUpdates() {
        List<Ride> activeRides = rideRepository.findAll().stream()
                .filter(ride -> ride.getStatus() == RideStatus.ACTIVE || ride.getStatus() == RideStatus.SCHEDULED)
                .toList();

        for (Ride ride : activeRides) {

            //add dummy passengers if there are none, for testing
//            if(ride.getPassengers() == null || ride.getPassengers().isEmpty()) {
//            ride.setPassengers( accountRepository.findAll().stream()
//                    .filter(account -> account.getRole().toString().equals("USER")).limit(1).toList() );
//            }

            if (ride.getPassengers() != null) {
                for (var passenger : ride.getPassengers()) {
                    RideTrackingUpdateDTO updateDTO = buildRideTrackingUpdate(ride);
                    messagingTemplate.convertAndSendToUser(
                            passenger.getId().toString(),
                            "/queue/ride-tracking",
                            updateDTO
                    );
                }
            }

            if (ride.getCreator() != null) {
                RideTrackingUpdateDTO updateDTO = buildRideTrackingUpdate(ride);
                messagingTemplate.convertAndSendToUser(
                        ride.getCreator().getId().toString(),
                        "/queue/ride-tracking",
                        updateDTO
                );
            }

            if (ride.getDriver() != null) {
                RideTrackingUpdateDTO updateDTO = buildRideTrackingUpdate(ride);
                messagingTemplate.convertAndSendToUser(
                        ride.getDriver().getId().toString(),
                        "/queue/ride-tracking",
                        updateDTO
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public void sendRideUpdateToUser(Long userId, Long rideId) {
        rideRepository.findById(rideId).ifPresent(ride -> {
            RideTrackingUpdateDTO updateDTO = buildRideTrackingUpdate(ride);
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/ride-tracking",
                    updateDTO
            );
        });
    }

    private RideTrackingUpdateDTO buildRideTrackingUpdate(Ride ride) {
        RideTrackingUpdateDTO dto = new RideTrackingUpdateDTO();
        dto.setRideId(ride.getId());
        dto.setStatus(ride.getStatus().toString());
        dto.setPrice(ride.getPrice());
        dto.setStartTime(ride.getStartTime());
        dto.setEstimatedEndTime(ride.getEndTime());

        if (ride.getEndTime() != null) {
            long minutesLeft = Duration.between(LocalDateTime.now(), ride.getEndTime()).toMinutes();
            long secondsLeft = Duration.between(LocalDateTime.now(), ride.getEndTime()).toSeconds() % 60;
            dto.setTimeLeft(String.format("%02d:%02d", Math.max(0, minutesLeft), Math.max(0, secondsLeft)));
        }

        //add dummy locations if there are none, for testing
//        if (ride.getLocations() != null && ride.getLocations().isEmpty()) {
//            Location location1 = new Location(19.8271,45.2580);
//            Location location2 = new Location(19.8339,45.2677);
//            ride.setLocations(List.of(location1, location2));
//        }

        if (ride.getLocations() != null && !ride.getLocations().isEmpty()) {
            Location currentLoc = ride.getLocations().get(0);
            dto.setCurrentLatitude(currentLoc.getLatitude());
            dto.setCurrentLongitude(currentLoc.getLongitude());

            List<RideTrackingUpdateDTO.LocationDTO> locationDTOs = ride.getLocations().stream()
                    .map(loc -> new RideTrackingUpdateDTO.LocationDTO(loc.getLatitude(), loc.getLongitude()))
                    .collect(Collectors.toList());
            dto.setRoute(locationDTOs);
        }

        if (ride.getDriver() != null && ride.getDriver().getVehicle() != null) {
            dto.setVehicleId(ride.getDriver().getVehicle().getId());
        }

        if (ride.getDriver() != null) {
            RideTrackingUpdateDTO.DriverInfoDTO driverInfo = new RideTrackingUpdateDTO.DriverInfoDTO();
            driverInfo.setId(ride.getDriver().getId());
            driverInfo.setName(ride.getDriver().getName() + " " + ride.getDriver().getLastName());
            driverInfo.setPhoneNumber(ride.getDriver().getPhoneNumber());
            dto.setDriver(driverInfo);
        }

        return dto;
    }
}

