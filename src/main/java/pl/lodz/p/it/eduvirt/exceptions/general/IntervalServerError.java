package pl.lodz.p.it.eduvirt.exceptions.general;

import pl.lodz.p.it.eduvirt.util.I18n;

public class IntervalServerError extends ApplicationBaseException {

    public IntervalServerError(String message) {
        super(message, I18n.INTERNAL_SERVER_ERROR);
    }
}
