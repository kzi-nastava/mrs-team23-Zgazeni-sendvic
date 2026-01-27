package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Vehicle;
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

    @PostMapping(
            value = "/vehicle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> registerVehicle(
            @RequestBody RegisterVehicleDTO dto
    ) {
        try {
            Vehicle vehicle = driverService.registerVehicle(dto);

            RegisteredVehicleDTO response = new RegisteredVehicleDTO();
            response.setId(vehicle.getId());
            response.setRegistration(vehicle.getRegistration());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping(path = "/changeStatus", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeDriverStatus(@RequestBody DriverChangeStatusDTO request)
            throws Exception {


        try {
            driverService.changeAvailableStatus(request.getEmail(), request.isToState());
            return ResponseEntity.ok("Status updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

    }

}


