package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("DRIVER")
public class Driver extends Account {

    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    public Driver() {
        super();
    }

    public Driver(Vehicle vehicle) {
        super();
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}

