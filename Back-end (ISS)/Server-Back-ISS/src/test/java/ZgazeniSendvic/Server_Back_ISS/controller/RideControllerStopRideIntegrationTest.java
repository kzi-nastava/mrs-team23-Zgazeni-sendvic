package ZgazeniSendvic.Server_Back_ISS.controller;

import ZgazeniSendvic.Server_Back_ISS.dto.OrsRouteResult;
import ZgazeniSendvic.Server_Back_ISS.dto.RideStopDTO;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.VehicleRepository;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.service.OrsRoutingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("RideController StopRide Integration Test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RideControllerStopRideIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RideRepository rideRepository;

    @MockitoBean
    private OrsRoutingService orsRoutingService;

    private OrsRouteResult mockRouteResult;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Driver testDriver;
    private Driver notDriverOfRide;
    private Account testPassenger;
    private Ride activeRide;
    private Ride scheduledRide;
    private Ride finishedRide;
    private Ride cancelledRide;
    private Location location1;
    private Location location2;
    private Location location3;

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

    @BeforeEach
    void setUp() {
        // Clean up database
        rideRepository.deleteAll();
        accountRepository.deleteAll();
        vehicleRepository.deleteAll();

        // Initialize locations
        location1 = new Location(20.4489, 44.8176); // Belgrade coordinates
        location2 = new Location(20.3971, 44.9199); // Novi Sad coordinates
        location3 = new Location(20.4500, 44.8500); // Another location

        // Create and save test passenger
        testPassenger = new User();
        testPassenger.setEmail("passenger@test.com");
        testPassenger.setPassword(passwordEncoder.encode("password123"));
        testPassenger.setName("John");
        testPassenger.setLastName("Passenger");
        testPassenger.setAddress("123 Passenger St, Test City");
        testPassenger.setPhoneNumber("1234567890");
        testPassenger.setConfirmed(true);
        testPassenger = accountRepository.save(testPassenger);

        // Create and save vehicle for testDriver
        Vehicle vehicle1 = new Vehicle(
                "Toyota Camry",
                "ABC-123",
                VehicleType.STANDARD,
                4,
                true,
                true
        );
        vehicle1 = vehicleRepository.save(vehicle1);

        // Create and save testDriver
        testDriver = new Driver(vehicle1);
        testDriver.setEmail("driver@test.com");
        testDriver.setPassword(passwordEncoder.encode("password123"));
        testDriver.setName("Michael");
        testDriver.setLastName("Driver");
        testDriver.setAddress("789 Driver Rd, Test City");
        testDriver.setPhoneNumber("5551234567");
        testDriver.setConfirmed(true);
        testDriver.setAvailable(true);
        testDriver.setActive(true);
        testDriver.setBusy(false);
        testDriver.setWorkedMinutesLast24h(0);
        testDriver = accountRepository.save(testDriver);

        // Create and save vehicle for notDriverOfRide
        Vehicle vehicle2 = new Vehicle(
                "Mercedes S-Class",
                "XYZ-789",
                VehicleType.LUXURY,
                4,
                false,
                false
        );
        vehicle2 = vehicleRepository.save(vehicle2);

        // Create and save notDriverOfRide
        notDriverOfRide = new Driver(vehicle2);
        notDriverOfRide.setEmail("notdriver@test.com");
        notDriverOfRide.setPassword(passwordEncoder.encode("password123"));
        notDriverOfRide.setName("Jane");
        notDriverOfRide.setLastName("NotDriver");
        notDriverOfRide.setAddress("456 NotDriver Ave, Test City");
        notDriverOfRide.setPhoneNumber("5559876543");
        notDriverOfRide.setConfirmed(true);
        notDriverOfRide.setAvailable(true);
        notDriverOfRide.setActive(true);
        notDriverOfRide.setBusy(false);
        notDriverOfRide.setWorkedMinutesLast24h(0);
        notDriverOfRide = accountRepository.save(notDriverOfRide);

        // Create active ride
        activeRide = new Ride();
        activeRide.setId(null);
        activeRide.setDriver(testDriver);
        activeRide.setCreator(testPassenger);
        activeRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        activeRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        )));
        activeRide.setTotalPrice(25.50);
        activeRide.setStartTime(LocalDateTime.now().minusMinutes(30));
        activeRide.setStatus(RideStatus.ACTIVE);
        activeRide.setPanic(false);
        activeRide = rideRepository.save(activeRide);




        // Create scheduled ride
        scheduledRide = new Ride();
        scheduledRide.setId(null);
        scheduledRide.setDriver(testDriver);
        scheduledRide.setCreator(testPassenger);
        scheduledRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        scheduledRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location1),
                returnNewLocation(location3)
        )));
        scheduledRide.setTotalPrice(30.00);
        scheduledRide.setStartTime(LocalDateTime.now().plusHours(1));
        scheduledRide.setStatus(RideStatus.SCHEDULED);
        scheduledRide.setPanic(false);
        scheduledRide = rideRepository.save(scheduledRide);


        // Create finished ride
        finishedRide = new Ride();
        finishedRide.setId(null);
        finishedRide.setDriver(testDriver);
        finishedRide.setCreator(testPassenger);
        finishedRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        finishedRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location2),
                returnNewLocation(location3)
        )));
        finishedRide.setTotalPrice(40.00);
        finishedRide.setStartTime(LocalDateTime.now().minusHours(2));
        finishedRide.setEndTime(LocalDateTime.now().minusHours(1));
        finishedRide.setStatus(RideStatus.FINISHED);
        finishedRide.setPanic(false);
        finishedRide = rideRepository.save(finishedRide);

        // Create cancelled ride
        cancelledRide = new Ride();
        cancelledRide.setId(null);
        cancelledRide.setDriver(testDriver);
        cancelledRide.setCreator(testPassenger);
        cancelledRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        cancelledRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location1),
                returnNewLocation(location3)
        )));
        cancelledRide.setTotalPrice(20.00);
        cancelledRide.setStartTime(LocalDateTime.now().plusHours(2));
        cancelledRide.setStatus(RideStatus.CANCELED);
        cancelledRide.setPanic(false);
        cancelledRide = rideRepository.save(cancelledRide);

        // Mock OrsRoutingService to avoid external API calls
        // Price calculation: distance (15000m = 15km) * 150 = 2250
        OrsRouteResult mockRouteResult = new OrsRouteResult(15000.0, 1200.0,
                List.of(List.of(20.4489, 44.8176), List.of(20.3971, 44.9199)), "driving-car");
        when(orsRoutingService.getFastestRouteWithPath(anyList())).thenReturn(mockRouteResult);
        this.mockRouteResult = mockRouteResult;

    }

    @Test
    @DisplayName("Should successfully stop active ride with valid request and DRIVER role")
    void testStopRide_Success() throws Exception {
        // Arrange
        LocalDateTime stopTime = LocalDateTime.now();
        List<Location> passedLocations = new ArrayList<>(Arrays.asList(
                returnNewLocation(location1),
                returnNewLocation(location2),
                returnNewLocation(location3)
        ));

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(passedLocations);
        stopRequest.setCurrentTime(stopTime);

        Long id = finishedRide.getId();
        Optional<Ride> activeOne = rideRepository.findById(id);

        // Act
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.rideID").value(activeRide.getId()))
                .andExpect(jsonPath("$.newPrice").value(mockRouteResult.getPrice()))
                .andReturn();

        // Assert - Check database state
        Optional<Ride> updatedRide = rideRepository.findById(activeRide.getId());
        assertThat(updatedRide).isPresent();
        assertThat(updatedRide.get().getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(updatedRide.get().getEndTime()).isNotNull();
        assertThat(updatedRide.get().getEndTime()).isEqualTo(stopTime);
        assertThat(updatedRide.get().getTotalPrice()).isEqualTo(mockRouteResult.getPrice());
        assertThat(updatedRide.get().getLocations()).hasSize(3);
    }

    @Test
    @DisplayName("Should return 404 when ride is not found")
    void testStopRide_RideNotFound() throws Exception {
        // Arrange
        Long nonExistentRideId = 999999L;
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", nonExistentRideId)
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isNotFound());

        // Assert - No changes should occur in database
        long rideCount = rideRepository.count();
        assertThat(rideCount).isEqualTo(4); // activeRide, scheduledRide, finishedRide, cancelledRide
    }

    @Test
    @DisplayName("Should return 400 when ride is not in ACTIVE status - SCHEDULED")
    void testStopRide_RideNotActive_Scheduled() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", scheduledRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isBadRequest());

        // Assert - Ride status should remain SCHEDULED
        Optional<Ride> unchangedRide = rideRepository.findById(scheduledRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.SCHEDULED);
        assertThat(unchangedRide.get().getEndTime()).isNull();
    }

    @Test
    @DisplayName("Should return 400 when ride is not in ACTIVE status - FINISHED")
    void testStopRide_RideNotActive_Finished() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", finishedRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isBadRequest());

        // Assert - Ride status should remain FINISHED
        Optional<Ride> unchangedRide = rideRepository.findById(finishedRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.FINISHED);
    }

    @Test
    @DisplayName("Should return 400 when ride is not in ACTIVE status - CANCELLED")
    void testStopRide_RideNotActive_Cancelled() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", cancelledRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isBadRequest());

        // Assert - Ride status should remain CANCELLED
        Optional<Ride> unchangedRide = rideRepository.findById(cancelledRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.CANCELED);
        assertThat(unchangedRide.get().getEndTime()).isNull();
    }

    @Test
    @DisplayName("Should return 403 when user is not the driver of the ride")
    void testStopRide_NotTheAssignedDriver() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(notDriverOfRide)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isForbidden());

        // Assert - Ride should remain ACTIVE and unchanged
        Optional<Ride> unchangedRide = rideRepository.findById(activeRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.ACTIVE);
        assertThat(unchangedRide.get().getEndTime()).isNull();
        assertThat(unchangedRide.get().getTotalPrice()).isEqualTo(25.50);
    }

    @Test
    @DisplayName("Should return 403 when user lacks DRIVER role - regular passenger")
    void testStopRide_NoDriverRole_Passenger() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testPassenger)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isForbidden());

        // Assert - No changes to database
        Optional<Ride> unchangedRide = rideRepository.findById(activeRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return 403 when user lacks DRIVER role - admin")
    void testStopRide_NoDriverRole_Admin() throws Exception {
        // Arrange
        Admin admin = new Admin();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setName("Admin");
        admin.setLastName("User");
        admin.setAddress("999 Admin Blvd, Test City");
        admin.setPhoneNumber("5555555555");
        admin.setConfirmed(true);
        admin = accountRepository.save(admin);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(admin)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isForbidden());

        // Assert - No changes to database
        Optional<Ride> unchangedRide = rideRepository.findById(activeRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return 401 when request is not authenticated")
    void testStopRide_Unauthenticated() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isUnauthorized());

        // Assert - No changes to database
        Optional<Ride> unchangedRide = rideRepository.findById(activeRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid - missing required fields")
    void testStopRide_InvalidRequestBody_MissingFields() throws Exception {
        // Arrange
        String invalidRequest = "{}"; // Missing required fields

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest))
                .andExpect(status().isBadRequest());

        // Assert - No changes to database
        Optional<Ride> unchangedRide = rideRepository.findById(activeRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid - empty locations")
    void testStopRide_InvalidRequestBody_EmptyLocations() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(new ArrayList<>()); // Empty list
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isBadRequest());

        // Assert - No changes to database
        Optional<Ride> unchangedRide = rideRepository.findById(activeRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid - null currentTime")
    void testStopRide_InvalidRequestBody_NullCurrentTime() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(null); // Null time

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isBadRequest());

        // Assert - No changes to database
        Optional<Ride> unchangedRide = rideRepository.findById(activeRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(RideStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return 400 when ride ID path variable is invalid")
    void testStopRide_InvalidRideId() throws Exception {
        // Arrange
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", "invalid-id")
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should correctly update locations in response and database")
    void testStopRide_LocationsInResponse() throws Exception {
        // Arrange
        List<Location> passedLocations = new ArrayList<>(Arrays.asList(
                returnNewLocation(location1),
                returnNewLocation(location2),
                returnNewLocation(location3)
        ));

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(passedLocations);
        stopRequest.setCurrentTime(LocalDateTime.now());

        Long id = activeRide.getId();
        Optional<Ride> activeOne = rideRepository.findById(id);

        // Act
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideID").value(activeRide.getId()))
                .andExpect(jsonPath("$.newPrice").value(mockRouteResult.getPrice()))
                .andExpect(jsonPath("$.updatedDestinations").isArray())
                .andExpect(jsonPath("$.updatedDestinations.length()").value(3));

        // Assert - Check database has updated locations
        Optional<Ride> updatedRide = rideRepository.findById(activeRide.getId());
        assertThat(updatedRide).isPresent();
        assertThat(updatedRide.get().getLocations()).hasSize(3);
        assertThat(updatedRide.get().getStatus()).isEqualTo(RideStatus.FINISHED);
    }

    @Test
    @DisplayName("Should accept valid ride with multiple passengers")
    void testStopRide_MultiplePassengers() throws Exception {
        // Arrange
        Account passenger2 = new User();
        passenger2.setEmail("passenger2@test.com");
        passenger2.setPassword(passwordEncoder.encode("password123"));
        passenger2.setName("Jane");
        passenger2.setLastName("Passenger2");
        passenger2.setAddress("456 Passenger Ave, Test City");
        passenger2.setPhoneNumber("9876543210");
        passenger2.setConfirmed(true);
        passenger2 = accountRepository.save(passenger2);

        // Update active ride to have multiple passengers
        activeRide.getPassengers().add(passenger2);
        activeRide = rideRepository.save(activeRide);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideID").value(activeRide.getId()))
                .andExpect(jsonPath("$.newPrice").value(mockRouteResult.getPrice()));

        // Assert - Check database state
        Optional<Ride> updatedRide = rideRepository.findById(activeRide.getId());
        assertThat(updatedRide).isPresent();
        assertThat(updatedRide.get().getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(updatedRide.get().getPassengers()).hasSize(2);
    }

    @Test
    @DisplayName("Should handle stopping ride with single location")
    void testStopRide_SingleLocation() throws Exception {
        // Arrange
        List<Location> singleLocation = new ArrayList<>(List.of(returnNewLocation(location1)));

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(singleLocation);
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideID").value(activeRide.getId()));

        // Assert - Check database state
        Optional<Ride> updatedRide = rideRepository.findById(activeRide.getId());
        assertThat(updatedRide).isPresent();
        assertThat(updatedRide.get().getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(updatedRide.get().getLocations()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle stopping ride with many locations")
    void testStopRide_ManyLocations() throws Exception {
        // Arrange
        List<Location> manyLocations = new ArrayList<>();
        manyLocations.add(returnNewLocation(location1));
        manyLocations.add(returnNewLocation(location2));
        manyLocations.add(returnNewLocation(location3));
        manyLocations.add(new Location(20.5000, 44.8000));
        manyLocations.add(new Location(20.5500, 44.8200));

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(manyLocations);
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updatedDestinations.length()").value(5));

        // Assert - Check database state
        Optional<Ride> updatedRide = rideRepository.findById(activeRide.getId());
        assertThat(updatedRide).isPresent();
        assertThat(updatedRide.get().getStatus()).isEqualTo(RideStatus.FINISHED);
        assertThat(updatedRide.get().getLocations()).hasSize(5);
    }

    @Test
    @DisplayName("Should correctly set end time when stopping ride")
    void testStopRide_EndTimeSet() throws Exception {
        // Arrange
        LocalDateTime stopTime = LocalDateTime.now();
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(stopTime);

        // Act
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk());

        // Assert - Check end time is set in database
        Optional<Ride> updatedRide = rideRepository.findById(activeRide.getId());
        assertThat(updatedRide).isPresent();
        assertThat(updatedRide.get().getEndTime()).isNotNull();
        assertThat(updatedRide.get().getEndTime()).isEqualTo(stopTime);
        assertThat(updatedRide.get().getEndTime()).isAfter(updatedRide.get().getStartTime());
    }

    @Test
    @DisplayName("Should update price based on routing service calculation")
    void testStopRide_PriceUpdate() throws Exception {
        // Arrange
        double originalPrice = activeRide.getTotalPrice();
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(testDriver)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPrice").value(mockRouteResult.getPrice()));

        // Assert - Check price is updated in database
        Optional<Ride> updatedRide = rideRepository.findById(activeRide.getId());
        assertThat(updatedRide).isPresent();
        assertThat(updatedRide.get().getTotalPrice()).isEqualTo(mockRouteResult.getPrice());
        assertThat(updatedRide.get().getTotalPrice()).isNotEqualTo(originalPrice);
    }

    @Test
    @DisplayName("Should handle different active ride from different driver")
    void testStopRide_DifferentActiveRide() throws Exception {
        // Arrange - Create another active ride with notDriverOfRide
        Ride anotherActiveRide = new Ride();
        anotherActiveRide.setId(null);
        anotherActiveRide.setDriver(notDriverOfRide);
        anotherActiveRide.setCreator(testPassenger);
        anotherActiveRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        anotherActiveRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location2),
                returnNewLocation(location3)
        )));
        anotherActiveRide.setTotalPrice(35.00);
        anotherActiveRide.setStartTime(LocalDateTime.now().minusMinutes(15));
        anotherActiveRide.setStatus(RideStatus.ACTIVE);
        anotherActiveRide.setPanic(false);
        anotherActiveRide = rideRepository.save(anotherActiveRide);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location2),
                returnNewLocation(location3)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        // Act - notDriverOfRide stops their own ride
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", anotherActiveRide.getId())
                                .with(user(new CustomUserDetails(notDriverOfRide)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideID").value(anotherActiveRide.getId()));

        // Assert - Check both rides exist with correct states
        Optional<Ride> updatedRide = rideRepository.findById(anotherActiveRide.getId());
        assertThat(updatedRide).isPresent();
        assertThat(updatedRide.get().getStatus()).isEqualTo(RideStatus.FINISHED);

        Optional<Ride> originalRide = rideRepository.findById(activeRide.getId());
        assertThat(originalRide).isPresent();
        assertThat(originalRide.get().getStatus()).isEqualTo(RideStatus.ACTIVE); // Still active
    }

    @Test
    @DisplayName("Should verify ride remains unchanged after failed stop attempt")
    void testStopRide_FailedAttempt_DatabaseUnchanged() throws Exception {
        // Arrange - Try to stop with wrong driver
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        ));
        stopRequest.setCurrentTime(LocalDateTime.now());

        double originalPrice = activeRide.getTotalPrice();
        RideStatus originalStatus = activeRide.getStatus();
        LocalDateTime originalStartTime = activeRide.getStartTime();

        // Act
        mockMvc.perform(
                        put("/api/ride-tracking/stop/{rideID}", activeRide.getId())
                                .with(user(new CustomUserDetails(notDriverOfRide)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stopRequest)))
                .andExpect(status().isForbidden());

        // Assert - Verify all fields remain unchanged
        Optional<Ride> unchangedRide = rideRepository.findById(activeRide.getId());
        assertThat(unchangedRide).isPresent();
        assertThat(unchangedRide.get().getStatus()).isEqualTo(originalStatus);
        assertThat(unchangedRide.get().getTotalPrice()).isEqualTo(originalPrice);
        assertThat(unchangedRide.get().getStartTime()).isEqualTo(originalStartTime);
        assertThat(unchangedRide.get().getEndTime()).isNull();
        assertThat(unchangedRide.get().getLocations()).hasSize(2); // Original size
    }

    public Location returnNewLocation(Location location) {
        return new Location(location.getLongitude(), location.getLatitude());
    }

}

