package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class EventNotFoundException extends NotFoundException {

    public EventNotFoundException(String message) {
        super(message, I18n.EVENT_NOT_FOUND);
    }
}
