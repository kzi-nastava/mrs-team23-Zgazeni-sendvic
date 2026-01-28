package ZgazeniSendvic.Server_Back_ISS.dto;

public class LoginRequestedDTO {

    private String token;
    private int expiresIn;
    private AccountLoginDTO user;

    public LoginRequestedDTO() {
    }

    public LoginRequestedDTO(String token, int expiresIn, AccountLoginDTO user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public AccountLoginDTO getUser() {
        return user;
    }

    public void setUser(AccountLoginDTO user) {
        this.user = user;
    }
}
