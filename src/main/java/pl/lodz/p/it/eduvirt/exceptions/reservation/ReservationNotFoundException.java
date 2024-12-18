package pl.lodz.p.it.eduvirt.exceptions.reservation;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationNotFoundException extends ReservationBaseException {

    public ReservationNotFoundException() {
        super(I18n.RESERVATION_NOT_FOUND);
    }

    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException(Throwable cause) {
        super(I18n.RESERVATION_NOT_FOUND, cause);
    }

    public ReservationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
