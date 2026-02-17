package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.RideEndDTO;
import ZgazeniSendvic.Server_Back_ISS.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RideControllerEndRideIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @Autowired(required = false)
    private ObjectMapper objectMapper;
    @MockitoBean
    private RideServiceImpl rideService;
    @MockitoBean
    private OrsRoutingService orsRoutingService;
    @MockitoBean
    private VehiclePositionsService vehiclePositionsService;
    @MockitoBean
    private NoteAddingService noteAddingService;
    @MockitoBean
    private RideDriverRatingService rideDriverRatingService;
    @MockitoBean
    private HistoryOfRidesService historyOfRidesService;
    @MockitoBean
    private PanicNotificationService panicNotificationService;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void rideEnd_shouldReturn200_whenValidRequest() throws Exception {
        RideEndDTO dto = new RideEndDTO(1L, 25.0);
        doNothing().when(rideService).endRide(any(RideEndDTO.class));

        mockMvc.perform(put("/api/ride-end")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(rideService, times(1)).endRide(any(RideEndDTO.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void rideEnd_shouldReturn403_whenUserIsNotDriver() throws Exception {
        RideEndDTO dto = new RideEndDTO(1L, 25.0);

        mockMvc.perform(put("/api/ride-end")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rideService);
    }

    @Test
    void rideEnd_shouldReturn401_whenNotAuthenticated() throws Exception {
        RideEndDTO dto = new RideEndDTO(1L, 25.0);

        mockMvc.perform(put("/api/ride-end")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rideService);
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void rideEnd_shouldReturn404_whenRideNotFound() throws Exception {
        RideEndDTO dto = new RideEndDTO(999L, 25.0);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"))
            .when(rideService).endRide(any(RideEndDTO.class));

        mockMvc.perform(put("/api/ride-end")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void rideEnd_shouldReturn400_whenInvalidRequest() throws Exception {
        RideEndDTO dto = new RideEndDTO(null, null);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request"))
            .when(rideService).endRide(any(RideEndDTO.class));

        mockMvc.perform(put("/api/ride-end")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void rideEnd_shouldReturn500_whenUnexpectedError() throws Exception {
        RideEndDTO dto = new RideEndDTO(1L, 25.0);
        doThrow(new RuntimeException("Database connection failed"))
            .when(rideService).endRide(any(RideEndDTO.class));

        mockMvc.perform(put("/api/ride-end")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void rideEnd_shouldAcceptRequestWithoutPrice() throws Exception {
        RideEndDTO dto = new RideEndDTO(1L, null);
        doNothing().when(rideService).endRide(any(RideEndDTO.class));

        mockMvc.perform(put("/api/ride-end")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(rideService, times(1)).endRide(any(RideEndDTO.class));
    }
}


