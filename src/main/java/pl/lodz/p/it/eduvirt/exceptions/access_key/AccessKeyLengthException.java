package pl.lodz.p.it.eduvirt.exceptions.access_key;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class AccessKeyLengthException extends BadRequestException {
    public AccessKeyLengthException() {
        super("Access key must be 5-50 characters long and contain only letters, numbers, hyphens and underscores", 
              I18n.ACCESS_KEY_INVALID_FORMAT);
    }
}
