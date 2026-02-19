package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.RideStopDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RideStoppedDTO;
import ZgazeniSendvic.Server_Back_ISS.exception.RideNotFoundException;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.service.OrsRoutingService;
import ZgazeniSendvic.Server_Back_ISS.service.RideServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;


@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RideControllerStopRideIntegrationTest")
public class RideControllerStopRideEndpointTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private RideServiceImpl rideService;

    @MockitoBean
    private OrsRoutingService orsRoutingService;

    @MockitoBean
    private RideRepository rideRepository;

    private Driver testDriver;
    private Account testAccount;
    private Ride testRide;
    private Location testLocation1;
    private Location testLocation2;
    private String driverToken;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize test data - based on DataLoader pattern
        testAccount = new User();
        testAccount.setEmail("accounta@test.com");
        testAccount.setPassword(passwordEncoder.encode("password123"));
        testAccount.setName("John");
        testAccount.setLastName("Doe");
        testAccount.setAddress("123 Main St, Test City");
        testAccount.setPhoneNumber("1234567890");
        testAccount.setConfirmed(true);

        // Create Vehicle
        Vehicle vehicle1 = new Vehicle(
                "Toyota Camry",
                "ABC-123",
                VehicleType.STANDARD,
                4,
                true,
                true
        );

        // Create Driver
        testDriver = new Driver(vehicle1);
        testDriver.setEmail("driver1@test.com");
        testDriver.setPassword(passwordEncoder.encode("password1234"));
        testDriver.setName("Michael");
        testDriver.setLastName("Johnson");
        testDriver.setAddress("789 Pine Rd, Test City");
        testDriver.setPhoneNumber("5551234567");
        testDriver.setConfirmed(true);
        testDriver.setAvailable(true);
        testDriver.setActive(true);
        testDriver.setBusy(false);
        testDriver.setWorkedMinutesLast24h(0);

        // Set testDriver ID using reflection
        java.lang.reflect.Field driverIdField = Account.class.getDeclaredField("id");
        driverIdField.setAccessible(true);
        driverIdField.set(testDriver, 1L);

        // Create Locations
        testLocation1 = new Location(20.4489, 44.8176);
        testLocation2 = new Location(20.3971, 44.9199);

        // Create Ride
        testRide = new Ride();
        testRide.setDriver(testDriver);
        testRide.setCreator(testAccount);

        List<Account> passengers = new ArrayList<>();
        passengers.add(testAccount);
        testRide.setPassengers(passengers);

        List<Location> locations = new ArrayList<>();
        locations.add(testLocation1);
        locations.add(testLocation2);
        testRide.setLocations(locations);

        testRide.setTotalPrice(25.50);
        testRide.setStartTime(LocalDateTime.now().minusHours(1));
        testRide.setStatus(RideStatus.ACTIVE);
        testRide.setPanic(false);
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");


    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("Should successfully stop ride with valid request and DRIVER role")
    void testStopRide_Success() throws Exception {
        // Arrange
        Long rideId = 1L;
        LocalDateTime stopTime = LocalDateTime.now();
        List<Location> passedLocations = new ArrayList<>();
        passedLocations.add(testLocation1);
        passedLocations.add(testLocation2);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(passedLocations);
        stopRequest.setCurrentTime(stopTime);

        RideStoppedDTO responseDTO = new RideStoppedDTO(rideId, 150.0, passedLocations);

        //Was failing because expecting a specific User defined object is quite hard to impossible
        when(rideService.stopRide(anyLong(), any(RideStopDTO.class)))
                .thenReturn(responseDTO);

        // Act & Assert
        MvcResult result = mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(testDriver)))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.rideID").value(rideId))
                .andExpect(jsonPath("$.newPrice").value(150.0))
                .andReturn();

        // Debug prints:

        verify(rideService, times(1)).stopRide(eq(rideId), any(RideStopDTO.class));
    }

    @Test
    @DisplayName("Should return 404 when ride is not found")
    void testStopRide_RideNotFound() throws Exception {
        // Arrange
        Long rideId = 999L;
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(testLocation1, testLocation2));
        stopRequest.setCurrentTime(LocalDateTime.now());

        when(rideService.stopRide(eq(999L), any(RideStopDTO.class)))
                .thenThrow(new RideNotFoundException("Ride was not found"));

        // Act & Assert
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(testDriver)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isNotFound());

        verify(rideService, times(1)).stopRide(eq(rideId), any(RideStopDTO.class));
    }

    @Test
    @DisplayName("Should return 400 when ride is not in ACTIVE status")
    void testStopRide_RideNotActive() throws Exception {
        // Arrange
        Long rideId = 1L;
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(testLocation1, testLocation2));
        stopRequest.setCurrentTime(LocalDateTime.now());

        when(rideService.stopRide(anyLong(), any(RideStopDTO.class)))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Ride is not active"));

        // Act & Assert
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(testDriver)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isBadRequest());

        verify(rideService, times(1)).stopRide(eq(rideId), any(RideStopDTO.class));
    }

    @Test
    @DisplayName("Should return 403 when user is not the driver of the ride")
    void testStopRide_NotDriver() throws Exception {
        // Arrange
        Long rideId = 1L;
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(testLocation1, testLocation2));
        stopRequest.setCurrentTime(LocalDateTime.now());

        Driver notDriver = new Driver();
        notDriver.setEmail("notdriver@test.com");
        notDriver.setName("Jane");
        notDriver.setLastName("Smith");

        java.lang.reflect.Field notDriverIdField = Account.class.getDeclaredField("id");
        notDriverIdField.setAccessible(true);
        notDriverIdField.set(notDriver, 2L);

        when(rideService.stopRide(anyLong(), any(RideStopDTO.class)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("Only the driver can stop the ride"));

        // Act & Assert
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(notDriver)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isForbidden());

        verify(rideService, times(1)).stopRide(eq(rideId), any(RideStopDTO.class));
    }

    @Test
    @DisplayName("Should return 403 (Forbidden) when user lacks DRIVER role")
    void testStopRide_NoDriverRole() throws Exception {
        // Arrange
        Long rideId = 1L;
        Account regularUser = new User();
        regularUser.setEmail("user@test.com");
        regularUser.setName("Regular");
        regularUser.setLastName("User");

        java.lang.reflect.Field userIdField = Account.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(regularUser, 3L);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(testLocation1, testLocation2));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert - Security should prevent this at annotation level
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(regularUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when request is not authenticated")
    void testStopRide_Unauthenticated() throws Exception {
        // Arrange
        Long rideId = 1L;
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(testLocation1, testLocation2));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert - No authentication setup
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid (missing required fields)")
    void testStopRide_InvalidRequestBody() throws Exception {
        // Arrange
        Long rideId = 1L;
        String invalidRequest = "{}"; // Missing required fields

        // Act & Assert
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(testDriver)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest()); // Bad request
    }

    @Test
    @DisplayName("Should return 400 when ride ID path variable is invalid")
    void testStopRide_InvalidRideId() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(testLocation1, testLocation2));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert - Invalid path parameter
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", "invalid-id")
                        .with(user(new CustomUserDetails(testDriver)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should correctly handle location data in response")
    void testStopRide_LocationsInResponse() throws Exception {
        // Arrange
        Long rideId = 1L;
        List<Location> passedLocations = new ArrayList<>();
        passedLocations.add(testLocation1);
        passedLocations.add(testLocation2);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(passedLocations);
        stopRequest.setCurrentTime(LocalDateTime.now());

        RideStoppedDTO responseDTO = new RideStoppedDTO(rideId, 200.0, passedLocations);

        when(rideService.stopRide(anyLong(), any(RideStopDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(testDriver)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideID").value(rideId))
                .andExpect(jsonPath("$.newPrice").value(200.0))
                .andExpect(jsonPath("$.updatedDestinations").isArray())
                .andExpect(jsonPath("$.updatedDestinations.length()").value(2));
    }

    @Test
    @DisplayName("Should handle exception from service and return appropriate HTTP status")
    void testStopRide_ServiceException() throws Exception {
        // Arrange
        Long rideId = 1L;
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(testLocation1, testLocation2));
        stopRequest.setCurrentTime(LocalDateTime.now());

        when(rideService.stopRide(anyLong(), any(RideStopDTO.class)))
                .thenThrow(new RuntimeException("Unexpected service error"));

        // Act & Assert
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(testDriver)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should accept valid ride ID and return response")
    void testStopRide_ValidRideResponse() throws Exception {
        // Arrange
        Long rideId = 42L;
        List<Location> locations = List.of(testLocation1, testLocation2);
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(locations);
        stopRequest.setCurrentTime(LocalDateTime.now());

        RideStoppedDTO responseDTO = new RideStoppedDTO(rideId, 175.50, locations);

        when(rideService.stopRide(eq(42L), any(RideStopDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(
                put("/api/ride-tracking/stop/{rideID}", rideId)
                        .with(user(new CustomUserDetails(testDriver)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideID").value(42))
                .andExpect(jsonPath("$.newPrice").value(175.50));
    }
}

