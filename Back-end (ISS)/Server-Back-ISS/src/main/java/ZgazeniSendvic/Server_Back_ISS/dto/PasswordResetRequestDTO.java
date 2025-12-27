package ZgazeniSendvic.Server_Back_ISS.dto;

public class PasswordResetRequestDTO {
    private String Email;

    public PasswordResetRequestDTO(String email) {
        Email = email;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
