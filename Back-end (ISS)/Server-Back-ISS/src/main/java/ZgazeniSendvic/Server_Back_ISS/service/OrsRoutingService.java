package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.OrsRouteResponse;
import ZgazeniSendvic.Server_Back_ISS.dto.OrsRouteResult;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrsRoutingService {

    @Autowired
    @Qualifier("orsWebClient")
    private WebClient orsWebClient;

    @Value("${ors.api.key}")
    private String orsApiKey;

    private final WebClient orsGeneric = WebClient.builder()
            .baseUrl("https://api.openrouteservice.org")
            .build();

    public OrsRouteResult getFastestRouteWithPath(List<List<Double>> waypoints) {

        try{
        if (waypoints == null || waypoints.size() < 2) {
            throw new IllegalArgumentException("At least 2 coordinates are required");
        }

        Map<String, Object> request = Map.of(
                "coordinates", waypoints
        );


        OrsRouteResponse response = orsWebClient.post() // Decide to send a POST
                .bodyValue(request) // Set the request as body
                .retrieve() // Actually send it, expecting a result
                .bodyToMono(OrsRouteResponse.class) // Transfer the JSON body into the ORSRouteResponse which mirrors it
                .block(); // Wait for answer, i.e. make it sync

        OrsRouteResponse.Route route = response.getRoutes().get(0);

        return new OrsRouteResult(
                route.getSummary().getDistance(),
                route.getSummary().getDuration(),
                route.getGeometry()
        );

        } catch (WebClientResponseException e) {
            // ORS responded with an error status (4xx, 5xx)
            System.err.println("ORS API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            // network issues, JSON parsing errors, etc.
            System.err.println("Error calling ORS API: " + e.getMessage());
        }

        return null;
    }

    public OrsRouteResult getFastestRouteWithLocations(List<Location> locations){
        List<List<Double>> waypoints = new ArrayList<>();
        for(Location location : locations){
            List<Double> coordinationSet = new ArrayList<>();
            coordinationSet.add(location.getLongitude());
            coordinationSet.add(location.getLatitude());

            waypoints.add(coordinationSet);


        }

        return getFastestRouteWithPath(waypoints);

    }




    public List<Double> addressToCordinates(String address) {
        try {
            //returns long - lat, assumed proper precision
            JsonNode result = orsGeneric.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/geocode/search")
                            .queryParam("api_key", orsApiKey)
                            .queryParam("text", address)
                            .queryParam("boundary.country", "RS")
                            .queryParam("size", 1)
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode firstFeature = result.get("features").get(0); //the first result, tho there is only one
            JsonNode coordsNode = firstFeature.path("geometry").path("coordinates");
            double lon = coordsNode.get(0).asDouble();
            double lat = coordsNode.get(1).asDouble();

            System.out.println(result);
            ArrayList<Double> coordinates = new ArrayList<>();
            coordinates.add(lon);
            coordinates.add(lat);
            return coordinates;





        } catch (Exception e) {
            System.err.println("Error calling ORS Geocode API: " + e.getMessage());
            return null;
        }

    }

    public OrsRouteResult getFastestRouteAddresses(List<String> addresses) {

        List<List<Double>> waypoints = new ArrayList<>();
        for (String address : addresses) {
            waypoints.add(addressToCordinates(address));
        }

        return getFastestRouteWithPath(waypoints);


    }

    /**
     Assumes the delimiter is ;
     @param allAddresses is simply all Addresses
     */
    public OrsRouteResult getFastestRouteAddresses(String allAddresses) {
        List<String> addresses = List.of(allAddresses.split(";"));
        return getFastestRouteAddresses(addresses);

    }

    public OrsRouteResult getFastestRouteAddresses(String firstAddress, String secondAddress) {
        List<String> addresses =  List.of(firstAddress, secondAddress);
        return getFastestRouteAddresses(addresses);
    }

}









