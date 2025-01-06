package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.util.I18n;

public class UsersNotFoundException extends UserBaseException {

    public UsersNotFoundException() {
        super(I18n.USERS_NOT_FOUND);
    }

    public UsersNotFoundException(String message) {
        super(message);
    }

    public UsersNotFoundException(Throwable cause) {
        super(I18n.USERS_NOT_FOUND, cause);
    }

    public UsersNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
