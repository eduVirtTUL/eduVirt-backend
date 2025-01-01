package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class MetricDeleteException extends BadRequestException {

    public MetricDeleteException(String message) {
        super(message, I18n.METRIC_DELETE_EXCEPTION);
    }
}
