package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.HistoryOfRidesDTO;
import ZgazeniSendvic.Server_Back_ISS.service.HistoryOfRidesService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/history-of-rides")
public class HistoryOfRidesController {
    @GetMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HistoryOfRidesDTO> HistoryOfRidesController( @PathVariable Long userId) {
        HistoryOfRidesService historyOfRidesService = new HistoryOfRidesService();
        HistoryOfRidesDTO historyOfRidesDTO = historyOfRidesService.getHistoryOfRides(userId);
        return ResponseEntity.ok(historyOfRidesDTO);
    }

}
