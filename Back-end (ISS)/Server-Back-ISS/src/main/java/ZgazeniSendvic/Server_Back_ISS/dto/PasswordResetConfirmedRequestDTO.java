package ZgazeniSendvic.Server_Back_ISS.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordResetConfirmedRequestDTO {

    @NotBlank
    private String email;
    @NotBlank
    private String code; // 6-digit code emailed to the user
    @NotBlank
    private String newPassword;

    public PasswordResetConfirmedRequestDTO() {
    }

    public PasswordResetConfirmedRequestDTO(String email, String code, String newPassword) {
        this.email = email;
        this.code = code;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
