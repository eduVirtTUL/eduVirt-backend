package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationEndBeforeStartException extends ReservationConflictException {

    public ReservationEndBeforeStartException() {
        super("Reservation end must happen after reservation start", I18n.RESERVATION_END_BEFORE_START);
    }
}
