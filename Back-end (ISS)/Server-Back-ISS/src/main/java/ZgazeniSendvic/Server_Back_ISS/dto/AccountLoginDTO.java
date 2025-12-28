package ZgazeniSendvic.Server_Back_ISS.dto;

import java.util.List;

public class AccountLoginDTO {

    private String email;
    private Long userID;
    private String firstName;
    private String lastName;
    private String pictUrl;
    private List<String> roles;

    // For writing ratings and such, the backend could always, based on ID, find the information required.
    // Since that's the case, the client needn't at all times know stuff such ass address/phoneNum

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

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
