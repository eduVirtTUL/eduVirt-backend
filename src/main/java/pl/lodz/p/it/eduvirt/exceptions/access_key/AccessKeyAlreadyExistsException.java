package pl.lodz.p.it.eduvirt.exceptions.access_key;

public class AccessKeyAlreadyExistsException extends AccessKeyBaseException {

    public AccessKeyAlreadyExistsException() {
        super("Access key already exists.");
    }

    public AccessKeyAlreadyExistsException(String message) {
        super(message);
    }

    public AccessKeyAlreadyExistsException(Throwable cause) {
        super("Access key already exists.", cause);
    }

    public AccessKeyAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
