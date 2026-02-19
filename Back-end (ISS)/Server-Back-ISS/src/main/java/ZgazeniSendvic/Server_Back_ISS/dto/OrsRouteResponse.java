package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.Data;

import java.util.List;

// Is supposed to mirror the JSON returned, so that the response is automatically converted to the class
// Might come in handy later
// Ors always returns one route, unless asked for alternatives as well, so effectively you're only working with one
@Data
public class OrsRouteResponse {

    private List<Route> routes;

    @Data
    public static class Route {
        private Summary summary;
        private String geometry;
    }

    @Data
    public static class Summary {
        private double distance; // meters
        private double duration; // seconds
    }


}
