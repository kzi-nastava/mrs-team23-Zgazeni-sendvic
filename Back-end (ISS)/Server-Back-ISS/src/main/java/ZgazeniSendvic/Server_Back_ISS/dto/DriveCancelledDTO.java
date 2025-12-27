package ZgazeniSendvic.Server_Back_ISS.dto;

//I will kep requester/rideIDs as any requester can cancel any ride, and they should all be aware, so its N-N

public class DriveCancelledDTO {

    private int requesterID;
    private int rideID;
    private String reason;
    private String time;
    private boolean isCancelled;

    public DriveCancelledDTO(){}
    public DriveCancelledDTO(int requesterID, int rideID, String reason, String time, boolean isCancelled) {
        this.requesterID = requesterID;
        this.rideID = rideID;
        this.reason = reason;
        this.time = time;
        this.isCancelled = isCancelled;
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

    public boolean isCancelled() {
        return isCancelled;
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

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }
}
