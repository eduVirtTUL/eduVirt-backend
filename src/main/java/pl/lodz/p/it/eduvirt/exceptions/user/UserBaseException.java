package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

public class UserBaseException extends ApplicationBaseException {

    public UserBaseException() {
    }

    public UserBaseException(String message) {
        super(message);
    }

    public UserBaseException(Throwable cause) {
        super(cause);
    }

    public UserBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
