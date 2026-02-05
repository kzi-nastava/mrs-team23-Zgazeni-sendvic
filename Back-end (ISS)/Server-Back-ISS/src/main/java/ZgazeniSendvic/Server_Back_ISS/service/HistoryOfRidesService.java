package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.HistoryOfRidesDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.PastRideDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
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
                String dep = r.getStartTime() == null ? null : r.getStartTime().toString();
                String arr = r.getEndTime() == null ? null : r.getEndTime().toString();
                double price =  r.getPrice();
                Location origin = null;
                Location dest = null;
//                if (r.getLocations().get(0) == null) {
//                    origin = new Location( 45.239576, 19.822779);
//                    dest = new Location(45.254582, 19.842490);
//                }else{
//                    origin = r.getLocations().get(0);
//                    dest = r.getLocations().get(r.getLocations().size() - 1);
//                }
                try{
                    origin = r.getLocations().get(0);
                    dest = r.getLocations().get(r.getLocations().size() - 1);
                }catch(IndexOutOfBoundsException e){
                    origin = new Location( 45.239576, 19.822779);
                    dest = new Location(45.254582, 19.842490);
                }
                PastRideDTO p = new PastRideDTO(r.getId(), origin, dest, dep, arr,
                        r.isPanic(), String.valueOf(r.isCanceled()), price);
                past.add(p);
            }
        }
        return new HistoryOfRidesDTO(past);
    }
}
