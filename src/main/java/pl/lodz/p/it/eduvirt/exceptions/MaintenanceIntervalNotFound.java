package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class MaintenanceIntervalNotFound extends NotFoundException {

    public MaintenanceIntervalNotFound(UUID maintenanceIntervalId) {
        super("Maintenance interval with id %s could not be found".formatted(maintenanceIntervalId), I18n.MAINTENANCE_INTERVAL_NOT_FOUND);
    }
}
