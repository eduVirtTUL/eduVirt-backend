package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ClusterMetricConflictException extends ConflictException {

    public ClusterMetricConflictException() {
        super("Cluster metric value could not be updated, since it was already updated by other user!",
                I18n.CLUSTER_METRIC_CONFLICT_EXCEPTION);
    }
}
