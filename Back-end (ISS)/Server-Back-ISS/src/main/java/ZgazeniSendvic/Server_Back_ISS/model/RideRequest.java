package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class RideRequest {

    @Getter @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    @ManyToOne
    private Account creator;

    @Getter @Setter
    @ElementCollection
    @CollectionTable(
            name = "ride_request_locations",
            joinColumns = @JoinColumn(name = "ride_request_id")
    )
    @OrderColumn(name = "idx")
    private List<Location> locations;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Getter @Setter
    private boolean babiesAllowed;
    @Getter @Setter
    private boolean petsAllowed;

    @Getter @Setter
    private LocalDateTime scheduledTime; // null = immediate

    @Getter @Setter
    @ManyToMany
    private List<Account> invitedPassengers;

    @Getter @Setter
    private double estimatedDistanceKm;
    @Getter @Setter
    private double estimatedPrice;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
