package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.Instant;

@Entity
@Table(
        name = "images",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "file_name")
        }
)
public class Image {

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

    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;


    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
