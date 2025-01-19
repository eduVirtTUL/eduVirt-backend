package pl.lodz.p.it.eduvirt.exceptions.user;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class UserNotAuthorizedException extends ForbiddenException {

    public UserNotAuthorizedException() {
        super("User does not posses the role required for this operation", I18n.USER_NOT_AUTHORIZED);
    }

    public UserNotAuthorizedException(String message) {
        super(message, I18n.USER_NOT_AUTHORIZED);
    }
}
