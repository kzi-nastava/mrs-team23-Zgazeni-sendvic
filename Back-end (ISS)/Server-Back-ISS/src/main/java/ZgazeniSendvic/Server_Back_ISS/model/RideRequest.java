package ZgazeniSendvic.Server_Back_ISS.model;

import java.util.ArrayList;

public class RideRequest {
    private Long id;
    private String start;
    private String destination;
    private ArrayList<String> midPoints;
    private String vehicleSelection;

    public RideRequest() { super(); }

    public RideRequest(Long id, String start, String destination, ArrayList<String> midPoints, String vehicleSelection) {
        this.id = id;
        this.start = start;
        this.destination = destination;
        this.midPoints = midPoints;
        this.vehicleSelection = vehicleSelection;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ArrayList<String> getMidPoints() {
        return midPoints;
    }

    public void setMidPoints(ArrayList<String> midPoints) {
        this.midPoints = midPoints;
    }

    public String getVehicleSelection() {
        return vehicleSelection;
    }

    public void setVehicleSelection(String vehicleSelection) {
        this.vehicleSelection = vehicleSelection;
    }
}
