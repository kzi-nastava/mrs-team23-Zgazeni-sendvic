package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.RideNoteDTO;
import ZgazeniSendvic.Server_Back_ISS.model.RideNote;
import ZgazeniSendvic.Server_Back_ISS.repository.RideNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class NoteAddingService {

    @Autowired
    RideNoteRepository repository;

    @Autowired
    RideServiceImpl rideServiceImpl;

    @Autowired
    AccountServiceImpl accountServiceImpl;

    @Transactional
    public boolean addNoteToRide(RideNoteDTO rideNoteDTO, Long userId) {
        try {
            rideServiceImpl.findRide(rideNoteDTO.getRideId());
            accountServiceImpl.findAccountById(userId);

            RideNote rn = new RideNote(rideNoteDTO.getRideId(), userId, rideNoteDTO.getNote(), OffsetDateTime.now());
            repository.save(rn);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to save ride note: ");
            e.printStackTrace();
            return false;
        }
    }
}
