package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ForbiddenException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationFinishException extends ForbiddenException {

    public ReservationFinishException(String message) {
        super(message, I18n.RESERVATION_DELETE_ERROR);
    }
}
