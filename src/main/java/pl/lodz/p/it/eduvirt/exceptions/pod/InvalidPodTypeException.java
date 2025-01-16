package pl.lodz.p.it.eduvirt.exceptions.pod;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class InvalidPodTypeException extends BadRequestException {

    public InvalidPodTypeException(String message) {
        super(message, I18n.POD_INVALID_TYPE);
    }
}
