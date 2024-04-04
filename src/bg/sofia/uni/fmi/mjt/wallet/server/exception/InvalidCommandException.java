package bg.sofia.uni.fmi.mjt.wallet.server.exception;

public class InvalidCommandException extends Throwable {
    public InvalidCommandException() {
        super("User enters an invalid command!");
    }

    public InvalidCommandException(String message) {
        super(message);
    }

    public InvalidCommandException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
