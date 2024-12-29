package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.util.I18n;

public class UserNotFoundException extends UserBaseException {

    public UserNotFoundException() {
        super(I18n.USER_NOT_FOUND);
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Throwable cause) {
        super(I18n.USER_NOT_FOUND, cause);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
