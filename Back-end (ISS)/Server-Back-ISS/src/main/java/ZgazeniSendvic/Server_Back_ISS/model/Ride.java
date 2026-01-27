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
    private Date finalDestTime;
    @Getter @Setter
    private Double latitude;
    @Getter @Setter
    private Double longitude;
    @Getter @Setter
    private boolean panic;
    @Getter @Setter
    private boolean canceled;
    @Getter @Setter
    private boolean started;
    @Getter @Setter
    private Double price;
    @Getter @Setter
    private boolean paid;
    @Getter @Setter
    private boolean ended;
    @Getter @Setter
    private Long driverId;
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
    private List<String> midpoints;

    //Perhaps there shouldn't be destination/origin/midpoints/locationsPassed
    //rather just locations, and then methods which return/edit only the first/last locations
    //perhaps shouldnt even be a list, but a string that gets converted to a list compile time

    public Ride() {}

    public Ride( String origin, String destination, Date departureTime, Date finalDestTime,
                Double latitude, Double longitude, boolean panic, boolean canceled, boolean started, Double price,
                List<String> locationsPassed) {
        //this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.finalDestTime = finalDestTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.panic = panic;
        this.canceled = canceled;
        this.started = started;
        this.price = price;
        midpoints = locationsPassed;
        this.paid = false;
        this.ended = false;
        this.driverId = null;
    }

    public void changeLocations(ArrayList<String> newLocations){
        midpoints = newLocations;

        if(!newLocations.isEmpty()){
            origin = newLocations.get(0);
        }
        if(newLocations.size() > 1){
            destination = newLocations.get(newLocations.size()-1);
        }

        if(newLocations.size() > 2){
            midpoints  = newLocations.subList(1, newLocations.size() - 1);
        }

        calculatePrice();

    }

    public double calculatePrice(){
        //would calc based on locations on implemented yet;
        price = 20.0;
        return price;
    }

    public ArrayList<String> getAllDestinations(){

        ArrayList<String> allDestinations = new ArrayList<>();
        allDestinations.add(origin);
        allDestinations.addAll(midpoints);
        allDestinations.add(destination);
        return allDestinations;

    }
}
