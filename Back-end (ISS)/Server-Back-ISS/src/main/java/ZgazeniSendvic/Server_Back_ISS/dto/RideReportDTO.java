package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RideReportDTO {
    private List<DailyRideReportDTO> dailyReports;
    private RideSummaryDTO summary;
}