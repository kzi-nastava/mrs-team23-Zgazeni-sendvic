package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.VehicleTrackerDTO;

public class VehicleTrackerService {
    public VehicleTrackerDTO getVehicleTrackingData(Long rideId) {
        Long vehicleId = 1L;
        Double latitude = 45.2671;
        Double longitude = 19.8335;
        String timeLeft = "00:15:30";

        return new VehicleTrackerDTO(vehicleId, latitude, longitude, timeLeft);
    }
}
