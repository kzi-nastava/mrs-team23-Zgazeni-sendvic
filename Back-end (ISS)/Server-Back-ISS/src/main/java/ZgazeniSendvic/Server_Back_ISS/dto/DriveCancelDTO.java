package ZgazeniSendvic.Server_Back_ISS.dto;


public class DriveCancelDTO {
    // has to contain info on who sent it
    private Long requesterID;
    private String reason;
    private String time;

    public DriveCancelDTO(){}
    public DriveCancelDTO(Long requesterID, String reason, String time) {
        this.requesterID = requesterID;
        this.reason = reason;
        this.time = time;
    }

    public void setRequesterID(Long requesterID) {
        this.requesterID = requesterID;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getRequesterID() {
        return requesterID;
    }

    public String getReason() {
        return reason;
    }

    public String getTime() {
        return time;
    }
}
