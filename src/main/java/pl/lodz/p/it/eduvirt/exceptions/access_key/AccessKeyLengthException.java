package pl.lodz.p.it.eduvirt.exceptions.access_key;

public class AccessKeyLengthException extends AccessKeyBaseException {

    public AccessKeyLengthException() {
        super("Access key length is invalid.");
    }

    public AccessKeyLengthException(String message) {
        super(message);
    }

    public AccessKeyLengthException(Throwable cause) {
        super("Access key length is invalid.", cause);
    }

    public AccessKeyLengthException(String message, Throwable cause) {
        super(message, cause);
    }
}
