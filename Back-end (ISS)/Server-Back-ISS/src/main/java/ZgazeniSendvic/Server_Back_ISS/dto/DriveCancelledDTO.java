package ZgazeniSendvic.Server_Back_ISS.dto;

//I will kep requester/rideIDs as any requester can cancel any ride, and they should all be aware, so its N-N

import java.time.LocalDate;

public class DriveCancelledDTO {

    private String requesterName;
    private Long rideID;
    private String reason;
    private String time;
    private boolean isCancelled;


    public DriveCancelledDTO() {



    }



    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }


    public Long getRideID() {
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

    public void setRideID(Long rideID) {
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
