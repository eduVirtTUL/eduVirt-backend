package pl.lodz.p.it.eduvirt.exceptions.maintenance_interval;

import pl.lodz.p.it.eduvirt.util.I18n;

public class MaintenanceIntervalNotFound extends MaintenanceIntervalBaseException {

    public MaintenanceIntervalNotFound() {
        super(I18n.MAINTENANCE_INTERVAL_NOT_FOUND);
    }

    public MaintenanceIntervalNotFound(String message) {
        super(message);
    }

    public MaintenanceIntervalNotFound(Throwable cause) {
        super(I18n.MAINTENANCE_INTERVAL_NOT_FOUND, cause);
    }

    public MaintenanceIntervalNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
