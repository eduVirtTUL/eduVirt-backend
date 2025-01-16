package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class InvalidAccessKeyTypeException extends ForbiddenException {
    public InvalidAccessKeyTypeException(String message) {
        super(message, I18n.ACCESS_KEY_INVALID_TYPE);
    }
}
