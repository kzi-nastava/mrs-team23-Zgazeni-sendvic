package ZgazeniSendvic.Server_Back_ISS.model;

import ZgazeniSendvic.Server_Back_ISS.dto.RegisterRequestDTO;
import jakarta.persistence.*;

@Entity
@Table(
        name = "account",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;
    private String lastName;
    private String address;
    private String phoneNumber;
    private String imgString;

    public Account() {
        super();
    }

    public Account(RegisterRequestDTO request){
        super();
        //this.id = id;
        this.email = request.getEmail();
        this.password = request.getPassword();
        this.name = request.getFirstName();
        this.lastName = request.getLastName();
        this.address = request.getAddress();
        this.phoneNumber = request.getPhoneNum();
        this.imgString = request.getPictUrl();
    }
    /* ---------- GETTERS / SETTERS ---------- */

    public Long getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getImgString() { return imgString; }
    public void setImgString(String imgString) { this.imgString = imgString; }

    public String getRole() {
        return this.getClass().getSimpleName().toUpperCase();
    }
}

