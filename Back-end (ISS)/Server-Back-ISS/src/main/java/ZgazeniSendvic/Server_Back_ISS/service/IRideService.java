package ZgazeniSendvic.Server_Back_ISS.service;


import ZgazeniSendvic.Server_Back_ISS.dto.DriveCancelDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.DriveCancelledDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.RideReportDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.RideRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface IRideService {
    public Collection<Ride> getAll();

    public Ride findRide(Long rideId);
    public Ride insert(Ride Ride);
    public Ride update(Long rideID, DriveCancelDTO rideDTO);
    public Ride delete(Long rideId);
    public void deleteAll();
    public DriveCancelledDTO updateCancel(Long rideID, DriveCancelDTO rideDTO);
    public Ride convertToRide(RideRequest request, Driver driver);

    RideReportDTO generateReport(
            Long accountId,
            LocalDateTime from,
            LocalDateTime to
    );
}

