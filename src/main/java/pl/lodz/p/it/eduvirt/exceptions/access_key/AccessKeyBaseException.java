package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;

public class AccessKeyBaseException extends RuntimeException {

    public AccessKeyBaseException() {
    }

    public AccessKeyBaseException(String message) {
        super(message);
    }

    public AccessKeyBaseException(Throwable cause) {
        super(cause);
    }

    public AccessKeyBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
