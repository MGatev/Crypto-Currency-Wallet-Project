package bg.sofia.uni.fmi.mjt.wallet.server.exception;

public class InvalidApiRequestException extends Throwable {

    public InvalidApiRequestException() {
        super("Invalid Request has been made and the reason is unknown.");
    }

    public InvalidApiRequestException(String message) {
        super(message);
    }

    public InvalidApiRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
