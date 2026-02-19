package ZgazeniSendvic.Server_Back_ISS.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PanicNotificationWebSocketController {

    @MessageMapping("/panic/subscribe")
    public void subscribeToPanicNotifications(String adminId, SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("adminId", adminId);
        }
        System.out.println("Admin " + adminId + " subscribed to panic notifications");
    }

    @MessageMapping("/panic/unsubscribe")
    public void unsubscribeFromPanicNotifications(String adminId) {
        System.out.println("Admin " + adminId + " unsubscribed from panic notifications");
    }
}

