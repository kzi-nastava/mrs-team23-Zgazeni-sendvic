package ZgazeniSendvic.Server_Back_ISS.dto;

public class PasswordResetRequestDTO {
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
