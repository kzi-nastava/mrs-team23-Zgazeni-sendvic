package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrsRouteResult {
    private double distanceMeters;
    private double durationSeconds;
    private List<List<Double>> pathCoordinates;
    private String type;

    public double getDistanceKm() { return distanceMeters / 1000.0; }
    public double getDurationMinutes() { return durationSeconds / 60.0; }
    public double getPrice() {
        double price = distanceMeters / 1000.0 * 150;
        return Math.round(price); // rounds to nearest whole number
    }
}
