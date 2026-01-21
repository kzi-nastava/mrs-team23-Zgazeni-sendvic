package ZgazeniSendvic.Server_Back_ISS.dto;


public class DriveCancelDTO {
    // has to contain info on who sent it
    private String requesterName;
    private String reason;
    private String time;

    public DriveCancelDTO(){}
    public DriveCancelDTO(String requesterID, String reason, String time) {
        this.requesterName = requesterID;
        this.reason = reason;
        this.time = time;
    }

    public void setRequesterID(String requesterID) {
        this.requesterName = requesterID;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRequesterID() {
        return requesterName;
    }

    public String getReason() {
        return reason;
    }

    public String getTime() {
        return time;
    }
}
