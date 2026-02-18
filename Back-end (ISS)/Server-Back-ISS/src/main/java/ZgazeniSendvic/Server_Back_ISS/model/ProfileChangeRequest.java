package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
public class ProfileChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Getter @Setter
    private Long accountId;

    @Getter @Setter
    private String newName;
    @Getter @Setter
    private String newLastName;
    @Getter @Setter
    private String newAddress;
    @Getter @Setter
    private String newPhoneNumber;
    @Getter @Setter
    private String newImgString;

    @Getter @Setter
    @Enumerated(EnumType.STRING)
    private RequestStatus status; // PENDING, ACCEPTED, REJECTED

    @Getter @Setter
    private LocalDateTime createdAt = LocalDateTime.now();
}

