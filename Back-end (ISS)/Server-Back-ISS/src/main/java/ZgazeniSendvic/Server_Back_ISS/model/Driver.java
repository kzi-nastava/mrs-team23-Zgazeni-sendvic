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
    @Getter @Setter
    Boolean available = false;
    @Getter @Setter
    Boolean driving = false;
    @Getter @Setter
    Boolean awaitingDeactivation = false; // would be better to keep this in a seperate table perhaps

    @Getter @Setter
    private Location location;

    @Getter @Setter
    private Boolean active = false;

    @Getter @Setter
    private Boolean busy = false;

    @Getter @Setter
    private Integer workedMinutesLast24h = 0;

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

    public void setAvailable(Boolean available) {
        this.available = available;
        //should awaiting be turned of regardgless? shouldnt ever occur that it should, but just in case
        awaitingDeactivation = false;
    }

    public boolean isAvailable() {
        return available != null && available;
    }

    void setDriving(Boolean driving) {
        if(driving != null && !driving){
            if(awaitingDeactivation != null && awaitingDeactivation){
                available = false;
                awaitingDeactivation = false;
            }
        }
        this.driving = driving;
    }

    public Boolean getDriving() {
        return driving;
    }

    public boolean isActive() {
        return active != null && active;
    }

    public boolean isBusy() {
        return busy;
    }
}

