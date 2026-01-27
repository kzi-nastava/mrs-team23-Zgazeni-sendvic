package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Location {

    private Double latitude;
    private Double longitude;

    public Location() {}

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
