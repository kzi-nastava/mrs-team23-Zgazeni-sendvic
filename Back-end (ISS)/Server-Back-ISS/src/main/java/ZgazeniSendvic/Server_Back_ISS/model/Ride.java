package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table
public class Ride {
    @Getter @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter @Setter
    private String origin;
    @Getter @Setter
    private String destination;
    @Getter @Setter
    private Date departureTime;
    @Getter @Setter
    private String timeLeft;
    @Getter @Setter
    private Double latitude;
    @Getter @Setter
    private Double longitude;
    @Getter @Setter
    private boolean panic;
    @Getter @Setter
    private boolean canceled;
    @Getter @Setter
    private Double price;
    //Commented so that implemting their repo's wont be needed for testing purposes
    /*@Getter @Setter
    @OneToOne(cascade={CascadeType.ALL})
    private Vehicle vehicle;
    @Getter @Setter
    @OneToMany(cascade={CascadeType.ALL})
    private List<Account> passengers;
    @Getter @Setter
    @OneToOne(cascade={CascadeType.ALL})
    private Account driver;
    */@Getter @Setter
    private List<String> LocationsPassed;

    public Ride() {}

    public Ride(Long id, String origin, String destination, Date departureTime, String timeLeft, Double latitude, Double longitude, boolean panic, Boolean canceled, Double price) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.timeLeft = timeLeft;
        this.latitude = latitude;
        this.longitude = longitude;
        this.panic = panic;
        this.canceled = canceled;
        this.price = price;
    }

    public Ride(Long id, String origin, String destination, Date departureTime, String timeLeft,
                Double latitude, Double longitude, boolean panic, boolean canceled, Double price,
                List<String> locationsPassed) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.timeLeft = timeLeft;
        this.latitude = latitude;
        this.longitude = longitude;
        this.panic = panic;
        this.canceled = canceled;
        this.price = price;
        this.LocationsPassed = locationsPassed;
    }

    public void changeLocations(ArrayList<String> newLocations){
        LocationsPassed = newLocations;

        if(!LocationsPassed.isEmpty()){
            destination = LocationsPassed.get(LocationsPassed.size() - 1);
            calculatePrice();
        }

    }

    public double calculatePrice(){
        //would calc based on locations on implemented yet;
        price = 20.0;
        return price;
    }

}
