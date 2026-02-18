package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.service.IRideRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RideRequestControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    IRideRequestService rideRequestService;

    @Test
    void requestRide_valid_returns201() throws Exception {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(10);
        dto.setScheduledTime(null);
        dto.setLocations(List.of(
                new Location(45, 19),
                new Location(45.1, 19.1)
        ));

        Mockito.when(rideRequestService.createRideRequest(any(CreateRideRequestDTO.class)))
                .thenReturn(new RideRequest());

        mockMvc.perform(post("/api/riderequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void requestRide_serviceThrows_returns500_or_400_dependsOnYourHandler() throws Exception {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(10);
        dto.setScheduledTime(LocalDateTime.now().plusHours(6));
        dto.setLocations(List.of(
                new Location(45, 19),
                new Location(45.1, 19.1)
        ));

        Mockito.when(rideRequestService.createRideRequest(any(CreateRideRequestDTO.class)))
                .thenThrow(new RuntimeException("Ride can only be scheduled up to 5 hours in advance."));

        mockMvc.perform(post("/api/riderequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                // ako imaš global handler koji mapira RuntimeException -> 400, stavi isBadRequest()
                .andExpect(status().isBadRequest());
    }
}
