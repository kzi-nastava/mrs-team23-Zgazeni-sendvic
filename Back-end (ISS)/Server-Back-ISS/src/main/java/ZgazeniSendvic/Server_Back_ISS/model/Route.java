package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_lng"))
    })
    private Location start;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "dest_lat")),
            @AttributeOverride(name = "longitude", column = @Column(name = "dest_lng"))
    })
    private Location destination;

    @ElementCollection
    @CollectionTable(name = "route_midpoints", joinColumns = @JoinColumn(name = "route_id"))
    private List<Location> midPoints = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Account owner;

    public Route() {}

    public Route(Location start, Location destination, List<Location> midPoints, Account owner) {
        this.start = start;
        this.destination = destination;
        this.midPoints = midPoints;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location getStart() {
        return start;
    }

    public void setStart(Location start) {
        this.start = start;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public List<Location> getMidPoints() {
        return midPoints;
    }

    public void setMidPoints(List<Location> midPoints) {
        this.midPoints = midPoints;
    }

    public Account getOwner() {
        return owner;
    }

    public void setOwner(Account owner) {
        this.owner = owner;
    }
}
