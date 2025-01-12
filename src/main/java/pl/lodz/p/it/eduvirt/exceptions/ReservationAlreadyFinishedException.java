package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationAlreadyFinishedException extends BadRequestException {

    public ReservationAlreadyFinishedException(String message) {
        super(message, I18n.RESERVATION_ALREADY_FINISHED);
    }
}
