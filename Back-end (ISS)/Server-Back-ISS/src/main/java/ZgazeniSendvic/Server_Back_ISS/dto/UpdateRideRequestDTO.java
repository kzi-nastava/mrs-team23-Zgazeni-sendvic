package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.ArrayList;

public class UpdateRideRequestDTO {
    private String start;
    private String destination;
    private ArrayList<String> midPoints;
    private String vehicleSelection;

    public UpdateRideRequestDTO() { super(); }

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
