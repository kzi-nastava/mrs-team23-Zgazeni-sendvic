package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;

@Entity
@AllArgsConstructor
@DiscriminatorValue("confirmation")
public class AccountConfirmationToken extends PasswordResetToken{
}
