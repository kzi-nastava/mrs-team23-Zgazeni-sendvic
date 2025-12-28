package ZgazeniSendvic.Server_Back_ISS.dto;

public class LoginRequestedDTO {

    private String token;
    private String tokenType;
    private AccountLoginDTO user;

    public LoginRequestedDTO() {
    }

    public LoginRequestedDTO(String token, String tokenType, AccountLoginDTO user) {
        this.token = token;
        this.tokenType = tokenType;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public AccountLoginDTO getUser() {
        return user;
    }

    public void setUser(AccountLoginDTO user) {
        this.user = user;
    }
}
