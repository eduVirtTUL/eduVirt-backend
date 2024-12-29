package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message, I18n.USER_NOT_FOUND);
    }
}
