package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ResourceGroupReservationCountExceededException extends BadRequestException {

    public ResourceGroupReservationCountExceededException(String message) {
        super(message, I18n.RESOURCE_GROUP_RESERVATION_COUNT_EXCEEDED);
    }
}
