package ZgazeniSendvic.Server_Back_ISS.dto;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class AccountLoginDTO {

    private String email;
    private Long userID;
    private String firstName;
    private String lastName;
    private String pictUrl;
    @Getter
    @Setter
    private String role;




    public AccountLoginDTO() {
    }

    public AccountLoginDTO(String email, Long userID, String firstName, String lastName,
                           String pictUrl) {
        this.email = email;
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pictUrl = pictUrl;
    }

    public AccountLoginDTO(Account account){
        this.email = account.getEmail();
        this.userID = account.getId();
        this.firstName = account.getName();
        this.lastName = account.getLastName();
        this.pictUrl = account.getImgString();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPictUrl() {
        return pictUrl;
    }

    public void setPictUrl(String pictUrl) {
        this.pictUrl = pictUrl;
    }
}
