package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class MetricNameAlreadyTakenException extends ConflictException {

    public MetricNameAlreadyTakenException(String message) {
        super(message, I18n.METRIC_NAME_ALREADY_TAKEN);
    }
}
