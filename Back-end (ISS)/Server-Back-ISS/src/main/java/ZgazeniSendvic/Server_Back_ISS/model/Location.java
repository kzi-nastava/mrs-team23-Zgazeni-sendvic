package ZgazeniSendvic.Server_Back_ISS.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Location {

    private Double latitude;
    private Double longitude;

    public Location() {}

    public Location(Double latitude, Double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(int lon, int lat) {
        super();
        this.latitude = Double.parseDouble(String.valueOf(lat));
        this.longitude = Double.parseDouble(String.valueOf(lon));
    }

    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
