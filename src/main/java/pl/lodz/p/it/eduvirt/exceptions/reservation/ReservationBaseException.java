package pl.lodz.p.it.eduvirt.exceptions.reservation;

import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

public class ReservationBaseException extends ApplicationBaseException {

    public ReservationBaseException() {
    }

    public ReservationBaseException(String message) {
        super(message);
    }

    public ReservationBaseException(Throwable cause) {
        super(cause);
    }

    public ReservationBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
