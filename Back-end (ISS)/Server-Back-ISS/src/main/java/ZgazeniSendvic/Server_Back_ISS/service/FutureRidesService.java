package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.FutureRidesDTO;

import ZgazeniSendvic.Server_Back_ISS.dto.NextRideDTO;
import java.util.List;

public class FutureRidesService {
    public FutureRidesDTO getFutureRides() {
        NextRideDTO nextRide1 = new NextRideDTO();
        nextRide1.setRideId(1L);
        nextRide1.setStartLocation("123 Main St");
        nextRide1.setEndLocation("456 Elm St");
        nextRide1.setDepartureTime("10:00:00");

        NextRideDTO nextRide2 = new NextRideDTO();
        nextRide2.setRideId(2L);
        nextRide2.setStartLocation("789 Oak St");
        nextRide2.setEndLocation("101 Pine St");
        nextRide2.setDepartureTime("12:00:00");

        List<NextRideDTO> futureRidesList = List.of(nextRide1, nextRide2);
        FutureRidesDTO futureRidesDTO = new FutureRidesDTO(futureRidesList);
        return futureRidesDTO;

    }
}
