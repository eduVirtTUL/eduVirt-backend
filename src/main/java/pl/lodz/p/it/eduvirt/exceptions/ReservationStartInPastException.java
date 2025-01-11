package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationStartInPastException extends ReservationConflictException {

    public ReservationStartInPastException() {
        super("New reservation cannot start in the past", I18n.RESERVATION_START_IN_PAST);
    }
}
