package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.util.I18n;

public class AccessKeyNotFoundException extends AccessKeyBaseException {

    public AccessKeyNotFoundException() {
        super(I18n.ACCESS_KEY_NOT_FOUND);
    }

    public AccessKeyNotFoundException(String message) {
        super(message);
    }

    public AccessKeyNotFoundException(Throwable cause) {
        super(I18n.ACCESS_KEY_NOT_FOUND, cause);
    }

    public AccessKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
