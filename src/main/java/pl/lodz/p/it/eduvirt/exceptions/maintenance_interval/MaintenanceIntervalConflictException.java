package pl.lodz.p.it.eduvirt.exceptions.maintenance_interval;

import pl.lodz.p.it.eduvirt.util.I18n;

public class MaintenanceIntervalConflictException extends MaintenanceIntervalBaseException {

    public MaintenanceIntervalConflictException() {
        super(I18n.MAINTENANCE_INTERVAL_CONFLICT);
    }

    public MaintenanceIntervalConflictException(String message) {
        super(message);
    }

    public MaintenanceIntervalConflictException(Throwable cause) {
        super(I18n.MAINTENANCE_INTERVAL_CONFLICT, cause);
    }

    public MaintenanceIntervalConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
