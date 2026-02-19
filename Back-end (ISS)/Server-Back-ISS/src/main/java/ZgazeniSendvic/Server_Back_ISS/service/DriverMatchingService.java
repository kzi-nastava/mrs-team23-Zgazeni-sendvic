package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Location;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.awt.geom.Point2D.distance;

@Service
public class DriverMatchingService {

    public Driver findBestDriver(List<Driver> drivers, Location origin) {

        // 1. Prefer free drivers
        // 2. Otherwise drivers finishing ≤10 min
        // 3. Pick closest

        return drivers.stream()
                .min(Comparator.comparing(d -> distance(d.getLocation().getLatitude(),
                        d.getLocation().getLongitude(), origin.getLatitude(), origin.getLongitude())))
                .orElse(null);
    }
}

