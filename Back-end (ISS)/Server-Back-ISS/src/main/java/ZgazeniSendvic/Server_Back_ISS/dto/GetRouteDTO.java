package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;

import java.util.ArrayList;

public class GetRouteDTO {
    private Long id;
    private Location start;
    private Location destination;
    private ArrayList<Location> midPoints;

    public GetRouteDTO() { super(); }

    public GetRouteDTO(Long id, Location start, Location destination, ArrayList<Location> midPoints) {
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
