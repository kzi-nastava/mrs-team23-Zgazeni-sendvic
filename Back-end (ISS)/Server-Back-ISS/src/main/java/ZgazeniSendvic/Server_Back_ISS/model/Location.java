package ZgazeniSendvic.Server_Back_ISS.model;

public class Location {
    private Long id;
    private Double longitude;
    private Double latitude;

    public Location() { super(); }

    public Location(Long id, Double longitude, Double latitude) {
        super();
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
