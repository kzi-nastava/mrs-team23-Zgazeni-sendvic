package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.Location;
import org.springframework.stereotype.Component;

@Component
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371;

    public double calculateDistanceKm(Location a, Location b) {

        double latDistance = Math.toRadians(b.getLatitude() - a.getLatitude());
        double lonDistance = Math.toRadians(b.getLongitude() - a.getLongitude());

        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());

        double aHarv = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(aHarv), Math.sqrt(1 - aHarv));

        return EARTH_RADIUS_KM * c;
    }
}

