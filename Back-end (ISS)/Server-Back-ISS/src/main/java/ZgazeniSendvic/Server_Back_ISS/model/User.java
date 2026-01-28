package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("USER")
public class User extends Account {
}
