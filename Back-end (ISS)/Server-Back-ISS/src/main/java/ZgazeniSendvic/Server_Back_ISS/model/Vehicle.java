package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "vehicle",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "registration")
        }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(nullable = false, unique = true)
    private String registration;

    private int numOfSeats;
    private Boolean babiesAllowed = false;
    private Boolean petsAllowed = false;
    @Getter
    @Setter
    private Boolean panicMark = false;

    public Vehicle() { }

    public Vehicle(
            String model,
            String registration,
            VehicleType type,
            int numOfSeats,
            Boolean babiesAllowed,
            Boolean petsAllowed
    ) {
        this.model = model;
        this.registration = registration;
        this.type = type;
        this.numOfSeats = numOfSeats;
        this.babiesAllowed = babiesAllowed;
        this.petsAllowed = petsAllowed;
    }

    /* ---------- GETTERS / SETTERS ---------- */

    public Long getId() { return id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }

    public String getRegistration() { return registration; }
    public void setRegistration(String registration) { this.registration = registration; }

    public int getNumOfSeats() { return numOfSeats; }
    public void setNumOfSeats(int numOfSeats) { this.numOfSeats = numOfSeats; }

    public boolean isBabiesAllowed() { return babiesAllowed != null && babiesAllowed; }
    public void setBabiesAllowed(Boolean babiesAllowed) { this.babiesAllowed = babiesAllowed; }

    public boolean isPetsAllowed() { return petsAllowed != null && petsAllowed; }
    public void setPetsAllowed(Boolean petsAllowed) { this.petsAllowed = petsAllowed; }
}
