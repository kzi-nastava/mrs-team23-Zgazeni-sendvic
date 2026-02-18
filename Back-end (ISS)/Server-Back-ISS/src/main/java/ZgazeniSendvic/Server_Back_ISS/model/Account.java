package ZgazeniSendvic.Server_Back_ISS.model;

import ZgazeniSendvic.Server_Back_ISS.dto.RegisterRequestDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "account",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        }
)
// auth.getPrincipal() returns your Account IF UserDetails is implemented. (as is in practice example)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 8)
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Size(min = 2)
    @Column(nullable = false, length = 100)
    private String name;
    @NotBlank
    @Size(min = 2)
    @Column(nullable = false, length = 100)
    private String lastName;
    @NotBlank
    @Column(nullable = false)
    private String address;
    @NotBlank
    @Pattern(regexp = "^\\d{10,}$")
    @Column(nullable = false, length = 20)
    private String phoneNumber;
    @Column(columnDefinition = "TEXT")
    private String imgString;
    @Getter @Setter
    private Boolean isConfirmed = false;

    public Account() {
        super();
    }

    public Account(Long id, String email, String password, String name, String lastName,
                   String address, String phoneNumber, String imgString) {
        super();
        //this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.imgString = imgString;
        isConfirmed = false;
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
        isConfirmed = false;
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

    public boolean isConfirmed() {
        return isConfirmed != null && isConfirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.isConfirmed = confirmed;
    }

    public String getRole() {
        return this.getClass().getSimpleName().toUpperCase();
    }

    public void setId(long l) {
        this.id = l;
    }
}

