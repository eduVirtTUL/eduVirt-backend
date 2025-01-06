package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class ClusterMetricNotFoundException extends NotFoundException {

    public ClusterMetricNotFoundException(UUID clusterId, UUID metricId) {
        super("Value of metric with id %s is not defined for cluster %s".formatted(metricId, clusterId),
                I18n.CLUSTER_METRIC_VALUE_NOT_DEFINED);
    }
}
