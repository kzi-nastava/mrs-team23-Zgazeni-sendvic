package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private boolean panic;

    @Getter @Setter
    private double totalPrice;

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
        locations = newLocations;
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
