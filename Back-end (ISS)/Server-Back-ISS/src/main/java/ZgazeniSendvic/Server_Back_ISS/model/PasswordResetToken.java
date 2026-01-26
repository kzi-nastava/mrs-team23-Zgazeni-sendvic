package ZgazeniSendvic.Server_Back_ISS.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tokenHash; // Hashed version


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account; // reference to Account

    @Column(nullable = false)
    private Instant expiresAt; // token expiry moment

    @Column(nullable = false)
    private boolean used = false; // for being single use


    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();


    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }
}
