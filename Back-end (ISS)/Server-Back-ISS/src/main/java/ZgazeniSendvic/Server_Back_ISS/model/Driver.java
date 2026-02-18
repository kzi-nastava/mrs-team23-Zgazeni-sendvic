package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("DRIVER")
public class Driver extends Account {

    @Getter @Setter
    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
    @Getter
    boolean available;
    boolean driving;
    @Getter @Setter
    boolean awaitingDeactivation; // would be better to keep this in a seperate table perhaps

    @Getter @Setter
    private Location location;

    @Getter @Setter
    private boolean active;

    @Getter @Setter
    private boolean busy;

    @Getter @Setter
    private int workedMinutesLast24h;

    @Getter @Setter
    @Column(unique = true)
    private String activationToken;

    public Driver() {
        super();
    }

    public Driver(Vehicle vehicle) {
        super();
        this.vehicle = vehicle;
    }

    //------------Status changes

    public void setAvailable(boolean available) {
        this.available = available;
        //should awaiting be turned of regardgless? shouldnt ever occur that it should, but just in case
        awaitingDeactivation = false;
    }

    void setDriving(boolean driving) {
        if(!driving){
            if(awaitingDeactivation){
                available = false;
                awaitingDeactivation = false;
            }
        }
        this.driving = driving;
    }

    public boolean getDriving() {
        return driving;
    }
}

