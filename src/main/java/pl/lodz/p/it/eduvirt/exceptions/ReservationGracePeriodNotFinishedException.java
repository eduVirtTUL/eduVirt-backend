package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationGracePeriodNotFinishedException extends BadRequestException {

    public ReservationGracePeriodNotFinishedException(String message) {
        super(message, I18n.POD_GRACE_PERIOD_NOT_FINISHED);
    }
}
