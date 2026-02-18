package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;


import java.time.Instant;

@Entity
@Table(
        name = "pictures",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "file_name")
        }
)
@Getter
@Setter
public class Picture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes")
    private long size;

    @OneToOne
    private Account owner;

    // The owner isnt even connected, only the image to the owner is

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
