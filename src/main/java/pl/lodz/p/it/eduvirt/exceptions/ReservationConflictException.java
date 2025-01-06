package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationConflictException extends ConflictException {

    public ReservationConflictException(String message) {
        super(message, I18n.RESERVATION_CONFLICT);
    }

    public ReservationConflictException(String message, String key) {
        super(message, key);
    }
}
