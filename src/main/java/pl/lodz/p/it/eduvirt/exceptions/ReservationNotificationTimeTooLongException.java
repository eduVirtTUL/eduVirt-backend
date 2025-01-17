package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationNotificationTimeTooLongException extends BadRequestException {

    public ReservationNotificationTimeTooLongException(String message) {
        super(message, I18n.RESERVATION_NOTIFICATION_TIME_TOO_LONG);
    }
}
