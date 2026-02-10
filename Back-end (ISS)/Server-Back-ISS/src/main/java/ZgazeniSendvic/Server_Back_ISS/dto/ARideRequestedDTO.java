package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flywaydb.core.internal.util.Locations;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ARideRequestedDTO {
    private Long rideID;
    private List<Location> destinations;
    private Location arrivingPoint;
    private Location endingPoint;
    private LocalDateTime beginning;
    private LocalDateTime ending;
    private RideStatus Status;
    private Long whoCancelled;
    private double price;
    private boolean panic;

}
