package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.HistoryOfRidesDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.PastRideDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
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
            if (r.getStatus().equals(RideStatus.FINISHED) && r.getDriver().getId() != null && r.getDriver().getId().equals(userId)) {
                String dep = r.getStartTime() == null ? null : fmt.format(r.getStartTime());
                String arr = r.getEndTime() == null ? null : fmt.format(r.getEndTime());
                double price =  r.getTotalPrice();
                PastRideDTO p = new PastRideDTO(r.getId(), r.getLocations().get(0),
                        r.getLocations().get(r.getLocations().size() - 1), dep, arr,
                        r.isPanic(), String.valueOf(r.isCanceled()), price);
                past.add(p);
            }
        }
        return new HistoryOfRidesDTO(past);
    }
}
