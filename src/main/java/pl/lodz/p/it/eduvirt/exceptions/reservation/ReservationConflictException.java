package pl.lodz.p.it.eduvirt.exceptions.reservation;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationConflictException extends ReservationBaseException {

    public ReservationConflictException() {
        super(I18n.RESERVATION_CONFLICT);
    }

    public ReservationConflictException(String message) {
        super(message);
    }

    public ReservationConflictException(Throwable cause) {
        super(I18n.RESERVATION_CONFLICT, cause);
    }

    public ReservationConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
