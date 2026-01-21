package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String model;
    private VehicleType type;
    private String registration;
    private int numOfSeats;
    private boolean babiesAllowed;
    private boolean petsAllowed;

    public Vehicle() { super(); }

    public Vehicle(String model, Long id, String registration, VehicleType type,
                   int numOfSeats, boolean babiesAllowed, boolean petsAllowed) {
        super();
        this.model = model;
        this.id = id;
        this.registration = registration;
        this.type = type;
        this.numOfSeats = numOfSeats;
        this.babiesAllowed = babiesAllowed;
        this.petsAllowed = petsAllowed;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getModel() { return model; }

    public void setModel(String model) { this.model = model; }

    public String getRegistration() { return registration; }

    public void setRegistration(String registration) { this.registration = registration; }

    public VehicleType getType() { return type; }

    public void setType(VehicleType type) { this.type = type; }

    public int getNumOfSeats() { return numOfSeats; }

    public void setNumOfSeats(int numOfSeats) { this.numOfSeats = numOfSeats; }

    public boolean isBabiesAllowed() { return babiesAllowed; }

    public void setBabiesAllowed(boolean babiesAllowed) { this.babiesAllowed = babiesAllowed; }

    public boolean isPetsAllowed() { return petsAllowed; }

    public void setPetsAllowed(boolean petsAllowed) { this.petsAllowed = petsAllowed; }
}
