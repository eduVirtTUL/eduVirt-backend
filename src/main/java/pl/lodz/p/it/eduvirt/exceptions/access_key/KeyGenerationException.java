package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class KeyGenerationException extends ApplicationBaseException {
    public KeyGenerationException() {
        super("Access key could not be generated", I18n.ACCESS_KEY_COULD_NOT_BE_GENERATED);
    }
}
