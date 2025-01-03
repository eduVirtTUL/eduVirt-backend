package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationMaxLengthExceededException extends BadRequestException {

    public ReservationMaxLengthExceededException(String message) {
        super(message, I18n.RESERVATION_MAX_LENGTH_EXCEEDED);
    }
}
