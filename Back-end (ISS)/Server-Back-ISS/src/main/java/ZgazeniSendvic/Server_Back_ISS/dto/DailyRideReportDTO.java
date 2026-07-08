package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyRideReportDTO {

    private LocalDate date;

    private int rideCount;

    private double distanceKm;

    private double totalPrice;
}