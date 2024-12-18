package pl.lodz.p.it.eduvirt.exceptions.reservation;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationEndBeforeStartException extends ReservationConflictException {

    public ReservationEndBeforeStartException() {
        super(I18n.RESERVATION_END_BEFORE_START);
    }

    public ReservationEndBeforeStartException(String message) {
        super(message);
    }

    public ReservationEndBeforeStartException(Throwable cause) {
        super(I18n.RESERVATION_END_BEFORE_START, cause);
    }

    public ReservationEndBeforeStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
