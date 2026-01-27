package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;

import java.util.List;

public class SaveRouteDTO {

    private Long ownerId;
    private Location start;
    private Location destination;
    private List<Location> midPoints;

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
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
}
