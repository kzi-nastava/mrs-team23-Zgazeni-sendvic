package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
import ZgazeniSendvic.Server_Back_ISS.model.RideRequest;
import ZgazeniSendvic.Server_Back_ISS.model.User;
import ZgazeniSendvic.Server_Back_ISS.model.VehicleType;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.service.IRideRequestService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RideRequestControllerIntegrationTest")
public class RideRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Keep the same ObjectMapper style as your other test
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private IRideRequestService rideRequestService;

    @Test
    @DisplayName("Should return 201 Created for valid ride request when authenticated")
    void requestRide_valid_returns201() throws Exception {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(10);
        dto.setScheduledTime(null);
        dto.setLocations(List.of(
                new Location(45, 19),
                new Location(45.1, 19.1)
        ));

        when(rideRequestService.createRideRequest(any(CreateRideRequestDTO.class)))
                .thenReturn(new RideRequest());

        // authenticated user (same style as your stop-ride tests)
        User testUser = new User();
        testUser.setEmail("user1@test.com");
        testUser.setName("User");
        testUser.setLastName("One");

        mockMvc.perform(post("/api/riderequest/create")
                        .with(user(new CustomUserDetails(testUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(rideRequestService, times(1)).createRideRequest(any(CreateRideRequestDTO.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when service rejects request (example rule: > 5h scheduling)")
    void requestRide_serviceThrows_returns400() throws Exception {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(10);
        dto.setScheduledTime(LocalDateTime.now().plusHours(6));
        dto.setLocations(List.of(
                new Location(45, 19),
                new Location(45.1, 19.1)
        ));

        when(rideRequestService.createRideRequest(any(CreateRideRequestDTO.class)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Ride can only be scheduled up to 5 hours in advance."
                ));

        User testUser = new User();
        testUser.setEmail("user1@test.com");
        testUser.setName("User");
        testUser.setLastName("One");

        mockMvc.perform(post("/api/riderequest/create")
                        .with(user(new CustomUserDetails(testUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(rideRequestService, times(1)).createRideRequest(any(CreateRideRequestDTO.class));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when request is not authenticated")
    void requestRide_unauthenticated_returns401() throws Exception {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(10);
        dto.setScheduledTime(null);
        dto.setLocations(List.of(
                new Location(45, 19),
                new Location(45.1, 19.1)
        ));

        mockMvc.perform(post("/api/riderequest/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
