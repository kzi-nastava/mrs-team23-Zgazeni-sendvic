package ZgazeniSendvic.Server_Back_ISS.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequestDTO {
    @NotBlank
    private String email;

    public PasswordResetRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
