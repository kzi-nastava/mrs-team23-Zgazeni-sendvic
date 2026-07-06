package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class RideReportItemDTO {

    private Long rideId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String driverName;

    private Location startLocation;

    private Location destinationLocation;

    private double distanceKm;

    private long durationMinutes;

    private double totalPrice;

    private RideStatus status;
}