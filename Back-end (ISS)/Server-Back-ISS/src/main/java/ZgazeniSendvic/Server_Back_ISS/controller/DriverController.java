package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.CreatedDriverDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.service.IDriverService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    @Autowired
    IDriverService driverService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createDriver(@RequestBody CreateDriverDTO dto) {

        try {
            Driver saved = driverService.registerDriver(dto);

            CreatedDriverDTO response = new CreatedDriverDTO();
            response.setId(saved.getId());
            response.setAccount(saved);
            response.setVehicle(saved.getVehicle());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
