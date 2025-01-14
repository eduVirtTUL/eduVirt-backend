package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ApplicationAccessDeniedException extends ForbiddenException {

    public ApplicationAccessDeniedException(String message) {
        super(message, I18n.ACCESS_DENIED_ERROR);
    }
}
