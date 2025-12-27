package ZgazeniSendvic.Server_Back_ISS.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ZgazeniSendvic.Server_Back_ISS.service.NextRideService;
import ZgazeniSendvic.Server_Back_ISS.dto.NextRideDTO;

@RestController
@RequestMapping("/api/next-ride")
public class NextRideController {
    @GetMapping(value = "/{filter}",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NextRideDTO> getNextRide(@PathVariable("filter") String filter) {
        NextRideService nextRideService = new NextRideService();
        NextRideDTO nextRideDTO = new NextRideDTO();
        if (filter.equals("1")) {
            nextRideDTO = nextRideService.getNextRideClosest();
        } else if (filter.equals("2")) {
            nextRideDTO = nextRideService.getNextRideCostliest();
        }
        return new ResponseEntity<NextRideDTO>(nextRideDTO, HttpStatus.OK);
    }
}
