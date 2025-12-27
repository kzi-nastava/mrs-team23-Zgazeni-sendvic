package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.service.NoteAddingService;
import ZgazeniSendvic.Server_Back_ISS.dto.RideNoteDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ride-noting-user")
public class RideNotingUserController {
    @PostMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> noteRide(@PathVariable("userId") Long userId, @RequestBody RideNoteDTO rideNoteDTO) {
        NoteAddingService noteAddingService = new NoteAddingService();
        boolean success = noteAddingService.addNoteToRide(rideNoteDTO.getRideId(), userId, rideNoteDTO.getNote());
        if (success) {
            System.out.println("Ride "+ rideNoteDTO.getRideId() +", note: " + rideNoteDTO.getNote());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(500).build();
        }
    }
}
