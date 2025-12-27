package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.HistoryOfRidesDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.PastRideDTO;

public class HistoryOfRidesService {

    public HistoryOfRidesDTO getHistoryOfRides(Long userId) {
        System.out.println("Getting history of rides for user: " + userId);

        PastRideDTO pastRideDTO1 = new PastRideDTO();
        pastRideDTO1.setId(1L);
        pastRideDTO1.setOrigin("Origin A");
        pastRideDTO1.setDestination("Destination B");
        pastRideDTO1.setDepartureTime("10:00:00");
        pastRideDTO1.setArrivalTime("11:00:00");
        pastRideDTO1.setPanic(false);
        pastRideDTO1.setCanceled("user124");
        pastRideDTO1.setPrice("15.00");

        PastRideDTO pastRideDTO2 = new PastRideDTO();
        pastRideDTO2.setId(2L);
        pastRideDTO2.setOrigin("Origin C");
        pastRideDTO2.setDestination("Destination D");
        pastRideDTO2.setDepartureTime("12:00:00");
        pastRideDTO2.setArrivalTime("13:00:00");
        pastRideDTO2.setPanic(true);
        pastRideDTO2.setCanceled("none");
        pastRideDTO2.setPrice("00.00");

        HistoryOfRidesDTO historyOfRidesDTO = new HistoryOfRidesDTO();
        historyOfRidesDTO.setRides(java.util.Arrays.asList(pastRideDTO1, pastRideDTO2));
        return historyOfRidesDTO;
    }
}
