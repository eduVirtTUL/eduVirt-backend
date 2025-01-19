package pl.lodz.p.it.eduvirt.exceptions.pod;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class PodAlreadyExistsException extends ConflictException {

    public PodAlreadyExistsException(String message) {
        super(message, I18n.POD_ALREADY_EXISTS);
    }
}
