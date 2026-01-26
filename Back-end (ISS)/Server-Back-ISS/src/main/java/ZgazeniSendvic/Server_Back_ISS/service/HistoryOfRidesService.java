package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.HistoryOfRidesDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.PastRideDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryOfRidesService {

    @Autowired
    private RideRepository rideRepository;

    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public HistoryOfRidesDTO getHistoryOfRides(Long userId) {
        List<Ride> all = rideRepository.findAll();
        List<PastRideDTO> past = new ArrayList<>();
        for (Ride r : all) {
            if (r.isEnded() && r.getDriverId() != null && r.getDriverId().equals(userId)) {
                String dep = r.getDepartureTime() == null ? null : fmt.format(r.getDepartureTime());
                String arr = r.getFinalDestTime() == null ? null : fmt.format(r.getFinalDestTime());
                String price =  r.getPrice() == null ? null : String.valueOf(r.getPrice());
                PastRideDTO p = new PastRideDTO(r.getId(), r.getOrigin(), r.getDestination(), dep, arr,
                        r.isPanic(), String.valueOf(r.isCanceled()), price);
                past.add(p);
            }
        }
        return new HistoryOfRidesDTO(past);
    }
}
