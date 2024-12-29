package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class ClusterInsufficientResourcesException extends ReservationCreationException {

    public ClusterInsufficientResourcesException(UUID clusterId) {
        super("Resources assigned to cluster %s are insufficient to create new reservation".formatted(clusterId),
                I18n.CLUSTER_RESOURCES_INSUFFICIENT);
    }
}
