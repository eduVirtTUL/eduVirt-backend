package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class UserNameAlreadyExistsException extends AlreadyExistsException {
    public UserNameAlreadyExistsException() {
        super("User with this username is already in the database", I18n.USER_NAME_ALREADY_EXISTS);
    }

    public UserNameAlreadyExistsException(String message) {
        super(message, I18n.USER_NAME_ALREADY_EXISTS);
    }
}
