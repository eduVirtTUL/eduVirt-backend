package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class PermissionNotFoundException extends NotFoundException {

    public PermissionNotFoundException(String message) {
        super(message, I18n.PERMISSION_NOT_FOUND);
    }
}
