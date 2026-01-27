package ZgazeniSendvic.Server_Back_ISS.model;

import ZgazeniSendvic.Server_Back_ISS.dto.RegisterRequestDTO;
import jakarta.persistence.*;
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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String name;
    private String lastName;
    private String address;
    private String phoneNumber;
    private String imgString;
    @Getter @Setter
    private boolean isConfirmed = false;

    /* ---------- ROLES ---------- */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id")
    )
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    public Account() {
        super();
        this.roles.add(Role.User);
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
        roles = new HashSet<>();
        roles.add(Role.User);
        isConfirmed = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    /* ---------- GETTERS / SETTERS ---------- */

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImgString() {
        return imgString;
    }

    public void setImgString(String imgString) {
        this.imgString = imgString;
    }



    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }


    public List<String> getRolesList() {
        List<String> rolesList = new ArrayList<>();
        for (Role role : roles) {
            rolesList.add(role.toString());
        }
        return rolesList;
    }



}
