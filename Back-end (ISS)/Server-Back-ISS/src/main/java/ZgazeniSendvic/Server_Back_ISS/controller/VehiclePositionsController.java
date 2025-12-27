package ZgazeniSendvic.Server_Back_ISS.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ZgazeniSendvic.Server_Back_ISS.dto.VehiclePositionDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.VehiclePositionsDTO;
import ZgazeniSendvic.Server_Back_ISS.service.VehiclePositionsService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-positions")
public class VehiclePositionsController {
    @Autowired
    private VehiclePositionsService vehiclePositionsService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehiclePositionsDTO> getVehiclePositions() {
        List<VehiclePositionDTO> vehiclePositions = vehiclePositionsService.getAllVehiclePositions();
        VehiclePositionsDTO responseDTO = new VehiclePositionsDTO(vehiclePositions);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}