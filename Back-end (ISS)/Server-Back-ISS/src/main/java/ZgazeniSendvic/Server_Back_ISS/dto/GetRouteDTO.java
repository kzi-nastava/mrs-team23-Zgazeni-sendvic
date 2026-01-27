package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Location;

import java.util.ArrayList;
import java.util.List;

public class GetRouteDTO {
    private Long id;
    private Location start;
    private Location destination;
    private List<Location> midPoints;
    private Account account;

    public GetRouteDTO() { super(); }

    public GetRouteDTO(Long id, Location start, Location destination, List<Location> midPoints) {
        super();
        this.id = id;
        this.start = start;
        this.destination = destination;
        this.midPoints = midPoints;
        this.account = new Account();
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
