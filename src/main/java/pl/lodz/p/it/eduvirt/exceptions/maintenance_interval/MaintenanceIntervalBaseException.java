package pl.lodz.p.it.eduvirt.exceptions.maintenance_interval;

import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

public class MaintenanceIntervalBaseException extends ApplicationBaseException {

    public MaintenanceIntervalBaseException() {
    }

    public MaintenanceIntervalBaseException(String message) {
        super(message);
    }

    public MaintenanceIntervalBaseException(Throwable cause) {
        super(cause);
    }

    public MaintenanceIntervalBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
