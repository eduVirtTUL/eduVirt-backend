package pl.lodz.p.it.eduvirt.exceptions.reservation;

public class ReservationCreationException extends ReservationBaseException {

    public ReservationCreationException() {
    }

    public ReservationCreationException(String message) {
        super(message);
    }

    public ReservationCreationException(Throwable cause) {
        super(cause);
    }

    public ReservationCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
