package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class AccessKeyAlreadyExistsException extends ConflictException {
    public AccessKeyAlreadyExistsException() {
        super("Access key already exists", I18n.ACCESS_KEY_ALREADY_EXISTS);
    }
}
