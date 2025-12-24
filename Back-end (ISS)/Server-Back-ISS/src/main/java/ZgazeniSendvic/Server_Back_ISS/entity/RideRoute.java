package ZgazeniSendvic.Server_Back_ISS.entity;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RideRoute {

    private List<String> routes = new ArrayList<>();
    @Getter
    private int totalTime = 0;

    public RideRoute() {
        // default constructor
    }

    public RideRoute(List<String> routes) {
        this.routes = new ArrayList<>(routes); // avoid external modification
        this.totalTime = calculateTravelTime();
    }

    public void setRoutes(List<String> routes) {
        this.routes = new ArrayList<>(routes); // avoid external modification
        this.totalTime = calculateTravelTime();
    }


    public List<String> getRoutes() {
        return new ArrayList<>(routes);
    }


    private int calculateTravelTime() {
        int time = 0;
        for (String destination : routes) {
            time += getMockTimeForDestination(destination);
        }
        return time;
    }


    private int getMockTimeForDestination(String destination) {
        return Math.abs(destination.hashCode()) % 30 + 10;
    }

    @Override
    public String toString() {
        return "RideRoute{" +
                "routes=" + routes +
                ", totalTime=" + totalTime +
                '}';
    }

}
