package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.CreateRideRequestDTO;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RideRequestServiceImplOrderRideTest {

    @Mock RideRequestRepository rideRequestRepository;
    @Mock AccountServiceImpl accountService;
    @Mock PricingService pricingService;
    @Mock DriverAssignmentService driverAssignmentService;

    @InjectMocks RideRequestServiceImpl service;

    private Account creator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        creator = new Account();
        creator.setId(10L);
        creator.setEmail("creator@test.com");
        when(accountService.getCurrentAccount()).thenReturn(creator);
    }

    @Test
    void createRideRequest_happyPath_savesPendingAndCallsAssignment() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(12.5);
        dto.setScheduledTime(null);
        dto.setLocations(List.of(
                new Location(45.0, 19.0),
                new Location(45.1, 19.1)
        ));
        dto.setInvitedPassengerEmails(List.of("p1@test.com", "p2@test.com"));

        // resolve passengers – najbolje je da je to u AccountServiceImpl
        Account p1 = new Account(); p1.setEmail("p1@test.com");
        Account p2 = new Account(); p2.setEmail("p2@test.com");
        when(accountService.resolveAccountsByEmails(dto.getInvitedPassengerEmails(), creator))
                .thenReturn(List.of(p1, p2));

        when(pricingService.calculatePrice(VehicleType.STANDARD, 12.5)).thenReturn(1520.0);

        // simulacija save
        ArgumentCaptor<RideRequest> captor = ArgumentCaptor.forClass(RideRequest.class);
        RideRequest saved = new RideRequest();
        saved.setId(77L);
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(saved);

        RideRequest result = service.createRideRequest(dto);

        verify(rideRequestRepository).save(captor.capture());
        RideRequest toSave = captor.getValue();

        assertEquals(RequestStatus.PENDING, toSave.getStatus());
        assertEquals(creator, toSave.getCreator());
        assertEquals(1520.0, toSave.getEstimatedPrice());
        assertEquals(2, toSave.getLocations().size());
        assertEquals(2, toSave.getInvitedPassengers().size());

        verify(driverAssignmentService).tryAssignDriver(77L);
        assertEquals(77L, result.getId());
    }

    @Test
    void createRideRequest_scheduleMoreThan5Hours_throws() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(5);
        dto.setLocations(List.of(
                new Location(45, 19),
                new Location(45.1, 19.1)
        ));
        dto.setScheduledTime(LocalDateTime.now().plusHours(6));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createRideRequest(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("5"));
        verifyNoInteractions(rideRequestRepository);
        verify(driverAssignmentService, never()).tryAssignDriver(anyLong());
    }

    @Test
    void createRideRequest_locationsLessThan2_throws() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(5);
        dto.setLocations(List.of(new Location(45, 19)));

        assertThrows(ResponseStatusException.class, () -> service.createRideRequest(dto));
        verifyNoInteractions(rideRequestRepository);
    }

    @Test
    void createRideRequest_invitedEmailNotFound_throws() {
        CreateRideRequestDTO dto = new CreateRideRequestDTO();
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setEstimatedDistanceKm(5);
        dto.setLocations(List.of(
                new Location(45, 19),
                new Location(45.1, 19.1)
        ));
        dto.setInvitedPassengerEmails(List.of("missing@test.com"));

        when(accountService.resolveAccountsByEmails(dto.getInvitedPassengerEmails(), creator))
                .thenThrow(new IllegalArgumentException("Passenger not found"));

        assertThrows(IllegalArgumentException.class, () -> service.createRideRequest(dto));
        verifyNoInteractions(rideRequestRepository);
    }
}

