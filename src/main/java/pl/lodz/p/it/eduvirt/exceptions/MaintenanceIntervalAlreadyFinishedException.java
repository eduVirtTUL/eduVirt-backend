package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class MaintenanceIntervalAlreadyFinishedException extends BadRequestException {

    public MaintenanceIntervalAlreadyFinishedException(String message) {
        super(message, I18n.MAINTENANCE_INTERVAL_ALREADY_FINISHED);
    }
}
