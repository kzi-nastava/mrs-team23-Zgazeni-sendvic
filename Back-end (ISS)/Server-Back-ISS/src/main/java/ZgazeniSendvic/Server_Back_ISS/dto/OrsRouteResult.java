package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrsRouteResult {
    private double distanceMeters;
    private double durationSeconds;
    private String pathCoordinates;

    public double getDistanceKm() { return distanceMeters / 1000.0; }
    public double getDurationMinutes() { return durationSeconds / 60.0; }
}
