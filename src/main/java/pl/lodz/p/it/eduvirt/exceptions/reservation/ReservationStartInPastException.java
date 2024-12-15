package pl.lodz.p.it.eduvirt.exceptions.reservation;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationStartInPastException extends ReservationConflictException {

    public ReservationStartInPastException() {
        super(I18n.RESERVATION_START_IN_PAST);
    }

    public ReservationStartInPastException(String message) {
        super(message);
    }

    public ReservationStartInPastException(Throwable cause) {
        super(I18n.RESERVATION_START_IN_PAST, cause);
    }

    public ReservationStartInPastException(String message, Throwable cause) {
        super(message, cause);
    }
}
