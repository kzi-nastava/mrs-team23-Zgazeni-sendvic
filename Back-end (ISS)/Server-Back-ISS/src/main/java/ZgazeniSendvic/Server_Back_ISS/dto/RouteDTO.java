package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Location;
import java.util.List;

public class RouteDTO {
    public Long id;
    public Location start;
    public Location destination;
    public List<Location> midPoints;
}

