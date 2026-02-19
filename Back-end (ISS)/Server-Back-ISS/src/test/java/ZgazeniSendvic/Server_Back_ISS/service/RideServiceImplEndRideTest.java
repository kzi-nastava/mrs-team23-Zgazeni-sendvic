package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.RideEndDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceImplEndRideTest {

    @Mock
    private RideRepository allRides;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private RideServiceImpl rideService;

    @Test
    void endRide_throwsBadRequest_whenDtoIsNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> rideService.endRide(null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(allRides);
        verifyNoInteractions(emailService);
    }

    @Test
    void endRide_throwsBadRequest_whenRideIdMissing() {
        RideEndDTO dto = new RideEndDTO();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> rideService.endRide(dto));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(allRides);
        verifyNoInteractions(emailService);
    }

    @Test
    void endRide_throwsNotFound_whenRideDoesNotExist() {
        RideEndDTO dto = new RideEndDTO(7L, 67.0);
        when(allRides.findById(7L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> rideService.endRide(dto));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(allRides).findById(7L);
        verifyNoInteractions(emailService);
    }

    @Test
    void endRide_marksRideFinished_andEmailsPassengers() {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setTotalPrice(10.0);
        ride.setStatus(RideStatus.ACTIVE);

        Account passenger1 = new Account();
        passenger1.setEmail("p1@gmail.com");
        Account passenger2 = new Account();
        passenger2.setEmail("p2@gmail.com");
        ride.setPassengers(new ArrayList<>(List.of(passenger1, passenger2)));

        when(allRides.findById(1L)).thenReturn(Optional.of(ride));

        RideEndDTO dto = new RideEndDTO(1L, 25.0);
        rideService.endRide(dto);

        assertEquals(25.0, ride.getTotalPrice());
        assertEquals(RideStatus.FINISHED, ride.getStatus());
        verify(allRides).save(ride);
        verify(allRides).flush();

        ArgumentCaptor<EmailDetails> emailCaptor = ArgumentCaptor.forClass(EmailDetails.class);
        verify(emailService, times(2)).sendSimpleMail(emailCaptor.capture());
        List<EmailDetails> sent = emailCaptor.getAllValues();
        assertEquals(List.of("p1@gmail.com", "p2@gmail.com"),
                List.of(sent.get(0).getRecipient(), sent.get(1).getRecipient()));
    }
}

