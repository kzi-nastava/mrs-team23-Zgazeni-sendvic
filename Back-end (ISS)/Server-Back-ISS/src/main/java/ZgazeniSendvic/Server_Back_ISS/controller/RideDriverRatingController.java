package ZgazeniSendvic.Server_Back_ISS.controller;


import ZgazeniSendvic.Server_Back_ISS.service.RideDriverRatingService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ZgazeniSendvic.Server_Back_ISS.dto.RideDriverRatingDTO;

@RestController
@RequestMapping("/api/ride-driver-rating")
public class RideDriverRatingController {
    @PostMapping(value="/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> rideDriverRating(@PathVariable("userId") Long userId, @RequestBody RideDriverRatingDTO rideDriverRatingDTO) {
        boolean success = RideDriverRatingService.saveRating(rideDriverRatingDTO);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(500).build();
        }
    }

}
