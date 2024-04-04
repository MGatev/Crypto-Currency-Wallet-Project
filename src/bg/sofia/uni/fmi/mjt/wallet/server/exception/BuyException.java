package bg.sofia.uni.fmi.mjt.wallet.server.exception;

public class BuyException extends Throwable {
    public BuyException() {
        super("Cannot buy the desired asset");
    }

    public BuyException(String message) {
        super(message);
    }

    public BuyException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
