package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class DuplicateKeyValueException extends ConflictException {
    public DuplicateKeyValueException(String keyValue) {
        super("Access key value '%s' already exists".formatted(keyValue), I18n.ACCESS_KEY_DUPLICATE);
    }
}
