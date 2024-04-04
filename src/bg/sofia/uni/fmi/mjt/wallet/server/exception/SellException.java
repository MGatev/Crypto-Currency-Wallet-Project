package bg.sofia.uni.fmi.mjt.wallet.server.exception;

public class SellException extends Throwable {
    public SellException() {
        super("Cannot sell the desired asset");
    }

    public SellException(String message) {
        super(message);
    }

    public SellException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
