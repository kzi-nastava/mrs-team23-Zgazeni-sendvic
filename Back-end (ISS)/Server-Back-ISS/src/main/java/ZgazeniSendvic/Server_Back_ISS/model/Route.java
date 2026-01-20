package ZgazeniSendvic.Server_Back_ISS.model;

import java.util.ArrayList;

public class Route {
    private Long id;
    private Location start;
    private Location destination;
    private ArrayList<Location> midPoints;
    //should a route have a single owner? multiple? any at all?
    //perhaps it having a single owner would allow for a manyToOne notation.
    //would be easer IMO

    public Route() { super(); }

    public Route(Long id, Location start, Location destination, ArrayList<Location> midPoints) {
        super();
        this.id = id;
        this.start = start;
        this.destination = destination;
        this.midPoints = midPoints;
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

    public ArrayList<Location> getMidPoints() {
        return midPoints;
    }

    public void setMidPoints(ArrayList<Location> midPoints) {
        this.midPoints = midPoints;
    }
}
