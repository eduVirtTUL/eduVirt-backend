package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.util.I18n;

public class InvalidKeyTypeException extends AccessKeyBaseException{

    public InvalidKeyTypeException() {
        super(I18n.INCORRECT_KEY_TYPE);
    }

    public InvalidKeyTypeException(String message) {
        super(message);
    }

    public InvalidKeyTypeException(Throwable cause) {
        super(I18n.INCORRECT_KEY_TYPE, cause);
    }

    public InvalidKeyTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
