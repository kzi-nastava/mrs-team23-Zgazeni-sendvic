package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.RideEndDTO;
import ZgazeniSendvic.Server_Back_ISS.service.RideEndService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ride-end")
public class RideEndController {
    @PutMapping(value="/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> rideEnd(@PathVariable("userId") Long userId, @RequestBody RideEndDTO rideEndDTO) {
        RideEndService rideEndService = new RideEndService();
        boolean success = rideEndService.RideEndService(rideEndDTO);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(500).build();
        }
    }
}
