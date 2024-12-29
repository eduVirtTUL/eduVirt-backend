package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.util.I18n;

public class DuplicateAccessKeyValueException extends AccessKeyBaseException {

    public DuplicateAccessKeyValueException() {
        super(I18n.DUPLICATE_KEY_VALUE);
    }

    public DuplicateAccessKeyValueException(String message) {
        super(message);
    }

    public DuplicateAccessKeyValueException(Throwable cause) {
        super(I18n.DUPLICATE_KEY_VALUE, cause);
    }

    public DuplicateAccessKeyValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
