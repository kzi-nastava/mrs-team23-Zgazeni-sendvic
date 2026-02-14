package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.OrsRouteResult;
import ZgazeniSendvic.Server_Back_ISS.dto.RideStopDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RideStoppedDTO;
import ZgazeniSendvic.Server_Back_ISS.exception.RideNotFoundException;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RideServiceStopRideTest")
public class RideServiceStopRideTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private OrsRoutingService orsRoutingService;

    @InjectMocks
    private RideServiceImpl rideService;

    private Driver testDriver;
    private Account testAccount;
    private Ride testRide;
    private Location testLocation1;
    private Location testLocation2;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize test data - based on DataLoader pattern
        // Create Account/Creator
        testAccount = new Account();
        testAccount.setEmail("accounta@test.com");
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
        testDriver.setName("Michael");
        testDriver.setLastName("Johnson");
        testDriver.setAddress("789 Pine Rd, Test City");
        testDriver.setPhoneNumber("5551234567");
        testDriver.setConfirmed(true);
        testDriver.setAvailable(true);
        testDriver.setActive(true);
        testDriver.setBusy(false);
        testDriver.setWorkedMinutesLast24h(0);

        // Set testDriver ID using reflection (id is inherited from Account)
        java.lang.reflect.Field driverIdField = Account.class.getDeclaredField("id");
        driverIdField.setAccessible(true);
        driverIdField.set(testDriver, 1L);

        // Create Locations
        testLocation1 = new Location(20.4489, 44.8176); // Belgrade, Serbia
        testLocation2 = new Location(20.3971, 44.9199); // Another location

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

        testRide.setPrice(25.50);
        testRide.setStartTime(LocalDateTime.now().minusHours(1));
        testRide.setStatus(RideStatus.ACTIVE);
        testRide.setPanic(false);
    }

    @Test
    @DisplayName("Should successfully stop ride when ride exists and is active and belongs to driver")
    void testStopRide_Success() {
        // Arrange
        Long rideId = 1L;
        LocalDateTime stopTime = LocalDateTime.now();
        List<Location> passedLocations = new ArrayList<>();
        passedLocations.add(testLocation1);
        passedLocations.add(testLocation2);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(passedLocations);
        stopRequest.setCurrentTime(stopTime);

        OrsRouteResult routeResult = new OrsRouteResult(10000.0, 600.0, new ArrayList<>(), "route");

        // Mock authentication
        setupSecurityContext(testDriver);

        // Mock repository and service calls
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(testRide));
        when(orsRoutingService.getFastestRouteWithPath(anyList())).thenReturn(routeResult);
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // Act
        RideStoppedDTO result = rideService.stopRide(rideId, stopRequest);

        // Assert
        assertNotNull(result);
        assertEquals(rideId, result.getRideID());
        assertEquals(routeResult.getPrice(), result.getNewPrice());
        assertEquals(passedLocations, result.getUpdatedDestinations());
        assertEquals(stopTime, testRide.getEndTime());
        verify(rideRepository, times(1)).findById(rideId);
        verify(orsRoutingService, times(1)).getFastestRouteWithPath(anyList());
        verify(rideRepository, times(1)).save(testRide);
        verify(rideRepository, times(1)).flush();
    }

    @Test
    @DisplayName("Should throw RideNotFound when ride is not found")
    void testStopRide_RideNotFound() {
        // Arrange
        Long rideId = 999L;
        RideStopDTO stopRequest = new RideStopDTO();

        setupSecurityContext(testAccount);
        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        // Act & Assert
        RideNotFoundException exception = assertThrows(RideNotFoundException.class, () -> {
            rideService.stopRide(rideId, stopRequest);
        });

        assertEquals("Ride was not found", exception.getMessage());
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    @ParameterizedTest
    @MethodSource("provideNonActiveRideStatuses")
    @DisplayName("Should throw ResponseStatusException when ride is not active")
    void testStopRide_RideNotActive(RideStatus status) {
        // Arrange
        Long rideId = 1L;
        testRide.setStatus(status);

        RideStopDTO stopRequest = new RideStopDTO();
        setupSecurityContext(testAccount);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(testRide));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            rideService.stopRide(rideId, stopRequest);
        });

        assertTrue(exception.getReason().contains("Ride is not active"));
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any());
    }

    static java.util.stream.Stream<RideStatus> provideNonActiveRideStatuses() {
        return java.util.stream.Stream.of(
                RideStatus.SCHEDULED,
                RideStatus.FINISHED,
                RideStatus.CANCELED
        );
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user is not the driver")
    void testStopRide_NotDriver() throws Exception {
        // Arrange
        Long rideId = 1L;

        // Set ID on testDriver using reflection (id is inherited from Account)
        java.lang.reflect.Field driverIdField = Account.class.getDeclaredField("id");
        driverIdField.setAccessible(true);
        driverIdField.set(testDriver, 1L);

        Driver notDriver = new Driver();
        notDriver.setEmail("notdriver@test.com");
        notDriver.setName("Jane");
        notDriver.setLastName("Smith");

        // Set ID on notDriver using reflection
        java.lang.reflect.Field notDriverIdField = Account.class.getDeclaredField("id");
        notDriverIdField.setAccessible(true);
        notDriverIdField.set(notDriver, 2L);

        RideStopDTO stopRequest = new RideStopDTO();
        setupSecurityContext(notDriver);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(testRide));

        // Act & Assert
        org.springframework.security.access.AccessDeniedException exception = assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> rideService.stopRide(rideId, stopRequest));

        assertTrue(exception.getMessage().contains("Only the driver can stop the ride"));
        verify(rideRepository, times(1)).findById(rideId);
        verify(rideRepository, never()).save(any());
    }


    @Test
    @DisplayName("Should update ride status to FINISHED after stopping")
    void testStopRide_StatusUpdatedToFinished() {
        // Arrange
        Long rideId = 1L;
        LocalDateTime stopTime = LocalDateTime.now();
        List<Location> passedLocations = new ArrayList<>();
        passedLocations.add(testLocation1);
        passedLocations.add(testLocation2);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(passedLocations);
        stopRequest.setCurrentTime(stopTime);

        OrsRouteResult routeResult = new OrsRouteResult(5000.0, 300.0, new ArrayList<>(), "route");

        setupSecurityContext(testDriver);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(testRide));
        when(orsRoutingService.getFastestRouteWithPath(anyList())).thenReturn(routeResult);
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // Act
        rideService.stopRide(rideId, stopRequest);

        // Assert
        assertEquals(RideStatus.FINISHED, testRide.getStatus());
        assertEquals(stopTime, testRide.getEndTime());
        verify(rideRepository, times(1)).save(testRide);
    }

    @Test
    @DisplayName("Should update ride price based on ORS routing service")
    void testStopRide_PriceUpdated() {
        // Arrange
        Long rideId = 1L;
        double expectedPrice = 150.0;
        LocalDateTime stopTime = LocalDateTime.now();
        List<Location> passedLocations = new ArrayList<>();
        passedLocations.add(testLocation1);
        passedLocations.add(testLocation2);

        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(passedLocations);
        stopRequest.setCurrentTime(stopTime);

        OrsRouteResult routeResult = new OrsRouteResult(1000.0, 600.0, new ArrayList<>(), "route");

        setupSecurityContext(testDriver);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(testRide));
        when(orsRoutingService.getFastestRouteWithPath(anyList())).thenReturn(routeResult);
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // Act
        rideService.stopRide(rideId, stopRequest);

        // Assert
        assertEquals(expectedPrice, testRide.getPrice());
        verify(orsRoutingService, times(1)).getFastestRouteWithPath(anyList());
    }

    @Test
    @DisplayName("Should call flush after saving ride")
    void testStopRide_FlushCalled() {
        // Arrange
        Long rideId = 1L;
        RideStopDTO stopRequest = new RideStopDTO();
        stopRequest.setPassedLocations(List.of(testLocation1, testLocation2));
        stopRequest.setCurrentTime(LocalDateTime.now());

        OrsRouteResult routeResult = new OrsRouteResult(5000.0, 300.0, new ArrayList<>(), "route");

        setupSecurityContext(testDriver);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(testRide));
        when(orsRoutingService.getFastestRouteWithPath(anyList())).thenReturn(routeResult);
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // Act
        rideService.stopRide(rideId, stopRequest);

        // Assert
        verify(rideRepository, times(1)).flush();
    }

    private void setupSecurityContext(Account account) {
        CustomUserDetails userDetails = new CustomUserDetails(account);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}










