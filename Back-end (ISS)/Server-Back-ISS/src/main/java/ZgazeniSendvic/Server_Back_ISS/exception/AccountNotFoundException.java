package ZgazeniSendvic.Server_Back_ISS.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("Account not found with id: " + id);
    }
}
