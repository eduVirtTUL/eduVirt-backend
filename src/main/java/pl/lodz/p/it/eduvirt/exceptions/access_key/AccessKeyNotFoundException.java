package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class AccessKeyNotFoundException extends NotFoundException {
    public AccessKeyNotFoundException() {
        super("Access key could not be found", I18n.ACCESS_KEY_NOT_FOUND);
    }
}
