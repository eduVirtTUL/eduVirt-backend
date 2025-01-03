package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class HostNotFoundException extends NotFoundException {

    public HostNotFoundException(String message) {
        super(message, I18n.HOST_NOT_FOUND);
    }
}
