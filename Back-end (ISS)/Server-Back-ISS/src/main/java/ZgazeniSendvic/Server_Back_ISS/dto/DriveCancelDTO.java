package ZgazeniSendvic.Server_Back_ISS.dto;

import org.springframework.stereotype.Component;


public class DriveCancelDTO {
    // has to contain info on who sent it, as well as ability to detect which ride it really is
    private int requesterID;
    private int rideID;
    private String reason;
    private String time;

    public DriveCancelDTO(){}
    public DriveCancelDTO(int requesterID, int rideID, String reason, String time) {
        this.requesterID = requesterID;
        this.rideID = rideID;
        this.reason = reason;
        this.time = time;
    }

    public void setRequesterID(int requesterID) {
        this.requesterID = requesterID;
    }

    public void setRideID(int rideID) {
        this.rideID = rideID;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getRequesterID() {
        return requesterID;
    }

    public int getRideID() {
        return rideID;
    }

    public String getReason() {
        return reason;
    }

    public String getTime() {
        return time;
    }
}
