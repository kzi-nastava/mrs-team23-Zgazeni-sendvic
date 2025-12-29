package ZgazeniSendvic.Server_Back_ISS.dto;

//I will kep requester/rideIDs as any requester can cancel any ride, and they should all be aware, so its N-N

import java.time.LocalDate;

public class DriveCancelledDTO {

    private String requesterName;
    private String requesterSecondName;
    private String startingDestination;
    private String finalDestination;
    private LocalDate beginningDate;
    private Long rideID;
    private String reason;
    private String time;
    private boolean isCancelled;


    public DriveCancelledDTO() {
    }

    public DriveCancelledDTO(String requesterName, String requesterSecondName, String startingDestination,
                             String finalDestination, LocalDate beginningDate, Long rideID, String reason,
                             String time, boolean isCancelled) {
        this.requesterName = requesterName;
        this.requesterSecondName = requesterSecondName;
        this.startingDestination = startingDestination;
        this.finalDestination = finalDestination;
        this.beginningDate = beginningDate;
        this.rideID = rideID;
        this.reason = reason;
        this.time = time;
        this.isCancelled = isCancelled;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterSecondName() {
        return requesterSecondName;
    }

    public void setRequesterSecondName(String requesterSecondName) {
        this.requesterSecondName = requesterSecondName;
    }

    public String getStartingDestination() {
        return startingDestination;
    }

    public void setStartingDestination(String startingDestination) {
        this.startingDestination = startingDestination;
    }

    public String getFinalDestination() {
        return finalDestination;
    }

    public void setFinalDestination(String finalDestination) {
        this.finalDestination = finalDestination;
    }

    public LocalDate getBeginningDate() {
        return beginningDate;
    }

    public void setBeginningDate(LocalDate beginningDate) {
        this.beginningDate = beginningDate;
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
