package ZgazeniSendvic.Server_Back_ISS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrsRouteResponseGeo {
    private List<Feature> features;

    @Data
    public static class Feature {
        private Properties properties;
        private Geometry geometry;
    }

    @Data
    public static class Properties {
        private Summary summary;
    }

    @Data
    public static class Summary {
        private double distance; // meters
        private double duration; // seconds
    }

    @Data
    public static class Geometry {
        private List<List<Double>> coordinates; // [longitude, latitude] pairs
        private String type; // "LineString"
    }
}
