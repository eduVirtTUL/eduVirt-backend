package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class MaintenanceIntervalInvalidTimeWindowException extends BadRequestException {

    public MaintenanceIntervalInvalidTimeWindowException(String message) {
        super(message, I18n.MAINTENANCE_INTERVAL_INVALID_TIME_WINDOW);
    }
}
