package ZgazeniSendvic.Server_Back_ISS.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import ZgazeniSendvic.Server_Back_ISS.service.FutureRidesService;
import ZgazeniSendvic.Server_Back_ISS.dto.FutureRidesDTO;

@RestController
@RequestMapping(value = "/api/future-rides",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class FutureRidesController {
    @GetMapping(value = "",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FutureRidesDTO> getFutureRides() {
        FutureRidesService futureRidesService = new FutureRidesService();
        FutureRidesDTO futureRidesDTO = futureRidesService.getFutureRides();
        return new ResponseEntity<FutureRidesDTO>(futureRidesDTO, HttpStatus.OK);
    }
}
