package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ReservationGracePeriodCouldNotFinishException extends BadRequestException {

    public ReservationGracePeriodCouldNotFinishException(String message) {
        super(message, I18n.POD_GRACE_PERIOD_COULD_NOT_FINISH);
    }
}
