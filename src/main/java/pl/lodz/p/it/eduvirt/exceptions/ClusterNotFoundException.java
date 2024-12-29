package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class ClusterNotFoundException extends NotFoundException {

    public ClusterNotFoundException(UUID clusterId) {
        super("Cluster with id %s could not be found".formatted(clusterId), I18n.CLUSTER_NOT_FOUND);
    }
}
