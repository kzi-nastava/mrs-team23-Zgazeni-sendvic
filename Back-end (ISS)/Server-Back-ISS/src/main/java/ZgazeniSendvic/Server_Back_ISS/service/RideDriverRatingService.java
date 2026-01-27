package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.RideDriverRatingDTO;
import ZgazeniSendvic.Server_Back_ISS.model.RideDriverRating;
import ZgazeniSendvic.Server_Back_ISS.repository.RideDriverRatingRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class RideDriverRatingService {

    @Autowired
    private RideDriverRatingRepository repository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RideRepository rideRepository;

    public boolean saveRating(RideDriverRatingDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating payload required");
        }
        if (dto.getUserId() == null || !accountRepository.existsById(dto.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (dto.getRideId() == null || !rideRepository.existsById(dto.getRideId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }
        try {
            RideDriverRating entity = new RideDriverRating(dto.getUserId(), dto.getRideId(), dto.getDriverRating(), dto.getVehicleRating(), dto.getComment());
            repository.save(entity);
            return true;
        } catch (Exception ex) {
            System.err.println("Failed to save ride driver rating: " + ex.getMessage());
            return false;
        }
    }
}
