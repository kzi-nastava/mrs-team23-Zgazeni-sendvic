package ZgazeniSendvic.Server_Back_ISS.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordResetConfirmedRequestDTO {

    @NotBlank
    private String token; //is embedded in the link or something
    @NotBlank
    private String newPassword;

    public PasswordResetConfirmedRequestDTO() {
    }

    public PasswordResetConfirmedRequestDTO(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
