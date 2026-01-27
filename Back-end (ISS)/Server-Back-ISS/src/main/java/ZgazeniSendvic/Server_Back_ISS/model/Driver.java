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
    private Location location;

    @Getter @Setter
    private boolean active;

    @Getter @Setter
    private boolean busy;

    @Getter @Setter
    private int workedMinutesLast24h;

    public Driver() {
        super();
    }

    public Driver(Vehicle vehicle) {
        super();
        this.vehicle = vehicle;
    }
}

