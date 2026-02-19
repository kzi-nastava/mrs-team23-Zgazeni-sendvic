package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RouteEstimationDTO {
    private List<String> routes = new ArrayList<>();
    @Getter
    private int totalTime = 0;

    public RouteEstimationDTO() {
        // default constructor
    }

    public RouteEstimationDTO(List<String> routes, int time) {
        this.routes = new ArrayList<>(routes); // avoid external modification
        this.totalTime = time;
    }



    public void setRoutes(List<String> routes) {
        this.routes = new ArrayList<>(routes);

    }


    public List<String> getRoutes() {
        return new ArrayList<>(routes);
    }




    @Override
    public String toString() {
        return "RideRoute{" +
                "routes=" + routes +
                ", totalTime=" + totalTime +
                '}';
    }
}
