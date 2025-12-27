package ZgazeniSendvic.Server_Back_ISS.module;

public class VehiclePosition{
    private Long id;
    private Double latitude;
    private Double longitude;
    private String status;

    public VehiclePosition() {
    }

    public VehiclePosition(Long id, Double latitude, Double longitude,String status) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}