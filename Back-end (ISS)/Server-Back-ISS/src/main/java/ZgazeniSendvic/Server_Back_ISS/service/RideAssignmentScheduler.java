package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.RequestStatus;
import ZgazeniSendvic.Server_Back_ISS.model.RideRequest;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideAssignmentScheduler {

    @Autowired
    RideRequestRepository rideRequestRepository;
    @Autowired
    DriverAssignmentService driverAssignmentService;
    @Autowired
    RideNotificationService notificationService;

    @Scheduled(fixedRate = 5000) // every 5 seconds
    @Transactional
    public void processPendingRideRequests() {

        List<RideRequest> pendingRequests =
                rideRequestRepository.findByStatus(RequestStatus.PENDING);

        for (RideRequest request : pendingRequests) {

            // Skip future rides not yet ready
            if (request.getScheduledTime() != null) {
                if (request.getScheduledTime()
                        .isAfter(LocalDateTime.now())) {
                    continue;
                }
            }

            driverAssignmentService.tryAssignDriver(request.getId());

            // If still pending after 1 minute → reject
            if (requestStillTimedOut(request)) {

                request.setStatus(RequestStatus.REJECTED);
                request.setRejectionReason("No drivers available.");
                rideRequestRepository.save(request);

                notificationService.sendNoDriversEmail(request.getCreator());
            }
        }
    }

    private boolean requestStillTimedOut(RideRequest request) {

        LocalDateTime created =
                request.getScheduledTime() == null
                        ? LocalDateTime.now().minusMinutes(1)
                        : request.getScheduledTime();

        return request.getStatus() == RequestStatus.PENDING
                && created.isBefore(LocalDateTime.now().minusMinutes(1));
    }
}
