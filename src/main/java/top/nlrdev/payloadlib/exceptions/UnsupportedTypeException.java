package top.nlrdev.payloadlib.exceptions;

public class UnsupportedTypeException extends RuntimeException {
    public UnsupportedTypeException(String message) {
        super(message);
    }

    public UnsupportedTypeException(Throwable cause) {
        super(cause);
    }

    public UnsupportedTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
