package bg.sofia.uni.fmi.mjt.wallet.server.exception;

public class LoginException extends Throwable {
    public LoginException() {
        super("Cannot login to the server.");
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
