package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;

public class BadRequestEduVirtException extends ApplicationBaseException {

    public BadRequestEduVirtException(String message) {
        super(message, "");
    }
}
