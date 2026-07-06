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

    @ElementCollection
    @CollectionTable(
            name = "ride_locations",
            joinColumns = @JoinColumn(name = "ride_id")
    )
    @OrderColumn(name = "idx")
    @Getter @Setter
    private List<Location> locations;

    @Getter @Setter
    private LocalDateTime scheduledTime;
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
    @Setter
    @Column(name = "start_latitude")
    @JsonIgnore
    private Double startLatitude;

    @Getter
    @Setter
    @Column(name = "start_longitude")
    @JsonIgnore
    private Double startLongitude;

    @Getter
    @Setter
    @Column(name = "end_latitude")
    @JsonIgnore
    private Double endLatitude;

    @Getter
    @Setter
    @Column(name = "end_longitude")
    @JsonIgnore
    private Double endLongitude;

    @Getter
    @Setter
    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Getter
    @Setter
    @Column(name = "current_longitude")
    private Double currentLongitude;


    @Getter @Setter
    private Boolean panic = false;

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

    @Getter @Setter
    private double totalPrice;

    @Getter @Setter
    private double distanceKm;

    public Ride(Long id, Driver driver, Account creator, List<Account> passengers, List<Location> locations,
                double price, LocalDateTime startTime, LocalDateTime endTime, RideStatus status, boolean panic) {
        this.id = id;
        this.driver = driver;
        this.creator = creator;
        this.passengers = passengers;
        this.locations = locations;
        this.totalPrice = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = calculateDuration();
        this.status = status;
        this.panic = panic;
    }

    public Ride() {}

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

    public double calculateDistanceKm() {
        if (locations == null || locations.size() < 2) return 0;

        double total = 0;

        for (int i = 0; i < locations.size() - 1; i++) {
            Location a = locations.get(i);
            Location b = locations.get(i + 1);

            total += haversine(
                    a.getLatitude(), a.getLongitude(),
                    b.getLatitude(), b.getLongitude()
            );
        }

        return total;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Earth radius in km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(Math.toRadians(lat1)) *
                                Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public boolean isThisPassenger(String email) {
        for (Account account : passengers) {
            if (account.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public void setPrice(double v) {
        this.totalPrice = v;
    }
}
