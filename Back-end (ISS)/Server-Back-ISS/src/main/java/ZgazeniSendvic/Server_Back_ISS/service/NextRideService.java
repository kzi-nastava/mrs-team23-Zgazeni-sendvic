package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.NextRideDTO;

public class NextRideService {
    public NextRideDTO getNextRideClosest() {
        NextRideDTO nextRide = new NextRideDTO();
        nextRide.setRideId(1L);
        nextRide.setStartLocation("123 Main St");
        nextRide.setEndLocation("456 Elm St");
        nextRide.setDepartureTime("10:00:00");
        return nextRide;
    }
    public NextRideDTO getNextRideCostliest() {
        NextRideDTO nextRide = new NextRideDTO();
        nextRide.setRideId(2L);
        nextRide.setStartLocation("789 Oak St");
        nextRide.setEndLocation("101 Pine St");
        nextRide.setDepartureTime("12:00:00");
        return nextRide;
    }
}
