package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RideTrackingUpdateDTO {
    private Long rideId;
    private Long vehicleId;
    private Double currentLatitude;
    private Double currentLongitude;
    private String status;
    private Double price;
    private LocalDateTime startTime;
    private LocalDateTime estimatedEndTime;
    private String timeLeft;
    private List<LocationDTO> route;
    private DriverInfoDTO driver;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDTO {
        private Double latitude;
        private Double longitude;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverInfoDTO {
        private Long id;
        private String name;
        private String phoneNumber;
    }
}

