package ZgazeniSendvic.Server_Back_ISS.websocket;

import ZgazeniSendvic.Server_Back_ISS.dto.RideEndedNotificationDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RideEndNotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendRideEndedNotification(Ride ride) {
        if (ride == null) {
            return;
        }

        RideEndedNotificationDTO payload = new RideEndedNotificationDTO(
                ride.getId(),
                ride.getStatus() != null ? ride.getStatus().toString() : null,
                ride.getTotalPrice(),
                ride.getEndTime()
        );

        Set<Long> notifiedUserIds = new HashSet<>();

        if (ride.getPassengers() != null) {
            for (Account passenger : ride.getPassengers()) {
                sendToUser(payload, passenger, notifiedUserIds);
            }
        }

        sendToUser(payload, ride.getCreator(), notifiedUserIds);
        sendToUser(payload, ride.getDriver(), notifiedUserIds);
    }

    private void sendToUser(RideEndedNotificationDTO payload, Account account, Set<Long> notifiedUserIds) {
        if (account == null || account.getId() == null) {
            return;
        }
        if (!notifiedUserIds.add(account.getId())) {
            return;
        }
        messagingTemplate.convertAndSendToUser(
                account.getId().toString(),
                "/queue/ride-ended",
                payload
        );
    }
}

