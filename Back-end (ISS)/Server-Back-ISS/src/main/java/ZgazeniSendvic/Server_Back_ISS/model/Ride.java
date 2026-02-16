package ZgazeniSendvic.Server_Back_ISS.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Ride {

    @Getter @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    @ManyToOne
    private Driver driver;

    @Getter @Setter
    @ManyToOne
    private Account creator;

    @Getter @Setter
    @ManyToMany
    private List<Account> passengers;

    @Getter @Setter
    @ElementCollection
    @OrderColumn
    private List<Location> locations;

    @Getter @Setter
    private double price;

    @Getter @Setter
    private LocalDateTime startTime;
    @Getter @Setter
    private LocalDateTime endTime;

    @Getter @Setter
    private Long durationMinutes;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    private RideStatus status;
    @Getter @Setter
    private LocalDateTime creationDate;
    @Getter @Setter
    @ManyToOne
    private Account canceler;
    // Sortable fields - hidden from JSON
    @Getter
    @Column(name = "start_latitude")
    @JsonIgnore
    private Double startLatitude;

    @Getter
    @Column(name = "start_longitude")
    @JsonIgnore
    private Double startLongitude;

    @Getter
    @Column(name = "end_latitude")
    @JsonIgnore
    private Double endLatitude;

    @Getter
    @Column(name = "end_longitude")
    @JsonIgnore
    private Double endLongitude;


    @Getter @Setter
    private boolean panic;

    @PrePersist
    public void prePersist() {
        if (this.creationDate == null) {
            this.creationDate = LocalDateTime.now();
        }
    }

    //So they never have to be updated manually, they will be updated on every update of the ride
    @PreUpdate
    public void preUpdate() {
        updateLocationCoordinates();
    }

    private void updateLocationCoordinates() {
        if (locations != null && !locations.isEmpty()) {
            Location firstLocation = locations.get(0);
            this.startLatitude = firstLocation.getLatitude();
            this.startLongitude = firstLocation.getLongitude();

            Location lastLocation = locations.get(locations.size() - 1);
            this.endLatitude = lastLocation.getLatitude();
            this.endLongitude = lastLocation.getLongitude();
        } else {
            this.startLatitude = null;
            this.startLongitude = null;
            this.endLatitude = null;
            this.endLongitude = null;
        }
    }

    public Ride(Long id, Driver driver, Account creator, List<Account> passengers, List<Location> locations,
                double price, LocalDateTime startTime, LocalDateTime endTime, RideStatus status, boolean panic) {
        this.id = id;
        this.driver = driver;
        this.creator = creator;
        this.passengers = passengers;
        this.locations = locations;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = calculateDuration();
        this.status = status;
        this.panic = panic;
    }

    public Ride() {
        this.id = 1L;
    }

    public boolean isCanceled() {
        return status == RideStatus.CANCELED;
    }

    public boolean isStarted() {
        return status == RideStatus.ACTIVE;
    }

    public boolean isPanic() { return panic; }

    public void changeLocations(ArrayList<Location> newLocations){
        this.locations.clear();
        this.locations.addAll(newLocations);

    }

    private long calculateDuration() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("endTime cannot be before startTime");
        }

        return Duration.between(startTime, endTime).toMinutes();
    }



    public boolean isThisPassenger(String email) {
        for (Account account : passengers) {
            if (account.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

}
