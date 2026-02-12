package ZgazeniSendvic.Server_Back_ISS.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RideTrackingWebSocketController {

    private final RideTrackingWebSocketService rideTrackingService;

    @MessageMapping("/ride-tracking/subscribe")
    public void subscribeToRideTracking(String userId, SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("userId", userId);
        }
        System.out.println("User " + userId + " subscribed to ride tracking");
    }

    @MessageMapping("/ride-tracking/unsubscribe")
    public void unsubscribeFromRideTracking(String userId) {
        System.out.println("User " + userId + " unsubscribed from ride tracking");
    }
}

