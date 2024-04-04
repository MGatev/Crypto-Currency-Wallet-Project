package bg.sofia.uni.fmi.mjt.wallet.server.exception;

public class InvalidAssetException extends Throwable {
    public InvalidAssetException() {
        super("Trying to work with invalid asset.");
    }

    public InvalidAssetException(String message) {
        super(message);
    }

    public InvalidAssetException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
