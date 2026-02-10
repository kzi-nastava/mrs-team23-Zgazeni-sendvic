package ZgazeniSendvic.Server_Back_ISS.exception;

public class RideTokenExpiredException extends RuntimeException {
    public RideTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
