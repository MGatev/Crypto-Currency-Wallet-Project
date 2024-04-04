package bg.sofia.uni.fmi.mjt.wallet.server.exception;

public class RegistrationException extends Throwable {
    public RegistrationException() {
        super("Cannot register on the server.");
    }

    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
