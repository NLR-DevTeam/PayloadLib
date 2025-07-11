package top.nlrdev.payloadlib.exceptions;

public class MalformedPayloadException extends RuntimeException {
    public MalformedPayloadException(String message) {
        super(message);
    }

    public MalformedPayloadException(Throwable cause) {
        super(cause);
    }

    public MalformedPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
