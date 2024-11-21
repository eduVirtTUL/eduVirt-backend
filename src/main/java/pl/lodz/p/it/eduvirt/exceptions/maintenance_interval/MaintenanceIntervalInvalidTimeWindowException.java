package pl.lodz.p.it.eduvirt.exceptions.maintenance_interval;

import pl.lodz.p.it.eduvirt.util.I18n;

public class MaintenanceIntervalInvalidTimeWindowException extends MaintenanceIntervalBaseException {

    public MaintenanceIntervalInvalidTimeWindowException() {
        super(I18n.MAINTENANCE_INTERVAL_INVALID_TIME_WINDOW);
    }

    public MaintenanceIntervalInvalidTimeWindowException(String message) {
        super(message);
    }

    public MaintenanceIntervalInvalidTimeWindowException(Throwable cause) {
        super(I18n.MAINTENANCE_INTERVAL_INVALID_TIME_WINDOW, cause);
    }

    public MaintenanceIntervalInvalidTimeWindowException(String message, Throwable cause) {
        super(message, cause);
    }
}
