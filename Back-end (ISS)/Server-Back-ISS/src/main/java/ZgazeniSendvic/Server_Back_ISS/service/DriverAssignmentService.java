package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverAssignmentService {

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    RideRequestRepository rideRequestRepository;
    @Autowired
    RideRepository rideRepository;
    @Autowired
    IRideService rideService;
    @Autowired
    RideNotificationService notificationService;
    @Autowired
    DistanceCalculator distanceCalculator;

    public void tryAssignDriver(Long requestId) {

        RideRequest request = rideRequestRepository.findById(requestId)
                .orElseThrow();

        // STOP if already handled
        if (request.getStatus() != RequestStatus.PENDING) {
            return;
        }

        List<Driver> activeDrivers = accountRepository.findAllActiveDrivers();

        if (activeDrivers.isEmpty()) {
            return; // don't reject immediately — cron will retry
        }

        // 8-hour rule
        List<Driver> eligibleDrivers = activeDrivers.stream()
                .filter(d -> d.getWorkedMinutesLast24h() < 480)
                .toList();

        if (eligibleDrivers.isEmpty()) {
            return;
        }

        Driver selectedDriver;

        if (request.getScheduledTime() == null) {
            selectedDriver = findBestImmediateDriver(request, eligibleDrivers);
        } else {
            selectedDriver = findBestScheduledDriver(request, eligibleDrivers);
        }

        if (selectedDriver == null) {
            return;
        }

        // SUCCESS
        Ride ride = rideService.convertToRide(request, selectedDriver);

        request.setStatus(RequestStatus.ACCEPTED);
        rideRequestRepository.save(request);

        selectedDriver.setBusy(true);
        selectedDriver.setAvailable(false);
        accountRepository.save(selectedDriver);

        notificationService.sendRideAcceptedEmail(request.getCreator(), ride);
        notificationService.sendNewRideForDriverEmail(selectedDriver, ride);
    }

    private Driver findBestImmediateDriver(
            RideRequest request,
            List<Driver> drivers) {

        Location start = request.getLocations().get(0);

        // Free drivers first
        List<Driver> freeDrivers = drivers.stream()
                .filter(Driver::isBusy)
                .toList();

        if (!freeDrivers.isEmpty()) {
            return findNearest(start, freeDrivers);
        }

        // Busy drivers finishing soon
        List<Driver> finishingSoon = drivers.stream()
                .filter(d -> {

                    Ride activeRide = rideRepository
                            .findActiveRideByDriver(d);

                    if (activeRide == null || activeRide.getEndTime() == null)
                        return false;

                    return Duration.between(
                            LocalDateTime.now(),
                            activeRide.getEndTime()
                    ).toMinutes() <= 10;

                })
                .toList();

        if (!finishingSoon.isEmpty()) {
            return findNearest(start, finishingSoon);
        }

        return null;
    }

    private Driver findBestScheduledDriver(RideRequest request, List<Driver> drivers) {

        Location startLocation = request.getLocations().get(0);

        // Scheduled rides can use free drivers first
        List<Driver> freeDrivers = drivers.stream()
                .filter(Driver::isBusy)
                .toList();

        if (!freeDrivers.isEmpty()) {
            return findNearest(startLocation, freeDrivers);
        }

        return null;
    }

    private Driver findNearest(Location start, List<Driver> drivers) {

        return drivers.stream()
                .min(Comparator.comparingDouble(d ->
                        distanceCalculator.calculateDistanceKm(start, d.getLocation())
                ))
                .orElse(null);
    }
}

