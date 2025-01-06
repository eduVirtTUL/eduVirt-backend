package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationCreationException extends BadRequestException {

    public ReservationCreationException(String message) {
        super(message, I18n.RESERVATION_CREATION_ERROR);
    }

    public ReservationCreationException(String message, String key) {
        super(message, key);
    }
}
