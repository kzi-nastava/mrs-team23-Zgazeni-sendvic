package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;

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
    private boolean babiesAllowed;
    private boolean petsAllowed;

    public Vehicle() { }

    public Vehicle(
            String model,
            String registration,
            VehicleType type,
            int numOfSeats,
            boolean babiesAllowed,
            boolean petsAllowed
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

    public boolean isBabiesAllowed() { return babiesAllowed; }
    public void setBabiesAllowed(boolean babiesAllowed) { this.babiesAllowed = babiesAllowed; }

    public boolean isPetsAllowed() { return petsAllowed; }
    public void setPetsAllowed(boolean petsAllowed) { this.petsAllowed = petsAllowed; }
}
