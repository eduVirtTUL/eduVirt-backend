package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class MaintenanceIntervalConflictException extends ConflictException {

    public MaintenanceIntervalConflictException(String message) {
        super(message, I18n.MAINTENANCE_INTERVAL_CONFLICT);
    }
}
