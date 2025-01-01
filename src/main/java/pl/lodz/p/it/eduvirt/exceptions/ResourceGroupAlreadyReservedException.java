package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ResourceGroupAlreadyReservedException extends AlreadyExistsException {

    public ResourceGroupAlreadyReservedException(String message) {
        super(message, I18n.RESOURCE_GROUP_ALREADY_RESERVED);
    }
}
