package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationStatusException extends BadRequestException {

    public ReservationStatusException(String message) {
        super(message, I18n.RESERVATION_STATUS_ALREADY_SET);
    }
}
