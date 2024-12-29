package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class MetricNotFoundException extends NotFoundException {

    public MetricNotFoundException(UUID metricId) {
        super("Metric with id %s could not be found".formatted(metricId), I18n.METRIC_NOT_FOUND);
    }
}
