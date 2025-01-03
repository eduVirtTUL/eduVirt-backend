package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class ClusterMetricExistsException extends AlreadyExistsException {

    public ClusterMetricExistsException(UUID clusterId, UUID metricId) {
        super("Value for metric with %s is already defined for cluster %s".formatted(metricId, clusterId), I18n.METRIC_VALUE_ALREADY_DEFINED);
    }
}
