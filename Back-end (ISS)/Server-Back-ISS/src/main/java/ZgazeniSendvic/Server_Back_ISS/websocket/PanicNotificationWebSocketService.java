package ZgazeniSendvic.Server_Back_ISS.websocket;

import ZgazeniSendvic.Server_Back_ISS.dto.PanicNotificationDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Admin;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PanicNotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AccountRepository accountRepository;

    /**
     * Sends panic notification to all admin users via WebSocket
     */
    public void sendPanicNotificationToAdmins(PanicNotificationDTO panicDTO) {
        // Send to ALL subscribers of /topic/panic
        messagingTemplate.convertAndSend("/topic/panic", panicDTO);
        System.out.println("Panic notification broadcast");
    }

    /**
     * Sends panic resolved notification to all admin users via WebSocket
     */
    public void sendPanicResolvedToAdmins(PanicNotificationDTO panicDTO) {
        List<Admin> admins = accountRepository.findAllAdmins();

        messagingTemplate.convertAndSend("/topic/panic/resolved", panicDTO);
        System.out.println("Panic notification broadcast");

        System.out.println("Panic resolved notification sent to " + admins.size() + " admins. Panic ID: " + panicDTO.getId());
    }
}



