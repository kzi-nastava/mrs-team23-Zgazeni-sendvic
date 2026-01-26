package ZgazeniSendvic.Server_Back_ISS.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class DriveCancelDTO {
    // has to contain info on who sent it
    private String requesterName;
    private String reason;
    private Date time;
    @Getter @Setter
    private String rideToken;

    public DriveCancelDTO(){}
    public DriveCancelDTO(String requesterID, String reason, Date time) {
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

    public void setTime(Date time) {
        this.time = time;
    }

    public String getRequesterID() {
        return requesterName;
    }

    public String getReason() {
        return reason;
    }

    public Date getTime() {
        return time;
    }
}
