package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "price",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "vehicle_type")
        }
)
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, unique = true)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private double price;

    public Price() {
    }

    public Price(VehicleType vehicleType, double price) {
        this.vehicleType = vehicleType;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
