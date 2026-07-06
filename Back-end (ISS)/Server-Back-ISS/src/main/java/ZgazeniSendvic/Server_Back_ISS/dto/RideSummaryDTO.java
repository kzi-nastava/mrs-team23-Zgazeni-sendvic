package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RideSummaryDTO {

    private int rideCount;

    private double totalDistanceKm;

    private long totalDurationMinutes;

    private double totalPrice;
}
