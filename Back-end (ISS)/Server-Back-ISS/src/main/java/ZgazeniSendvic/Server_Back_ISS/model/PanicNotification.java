package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "panic_notification")
public class PanicNotification {

    @Getter @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    @ManyToOne
    private Account caller;

    @Getter @Setter
    @OneToOne
    private Ride ride;

    @Getter @Setter
    private LocalDateTime createdAt;

    @Getter @Setter
    private boolean resolved;

    public PanicNotification() {
        this.resolved = false;
    }

    public PanicNotification(Account caller, Ride ride, LocalDateTime createdAt) {
        this.caller = caller;
        this.ride = ride;
        this.createdAt = createdAt;
        this.resolved = false;
    }
}



