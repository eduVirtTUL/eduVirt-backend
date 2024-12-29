package pl.lodz.p.it.eduvirt.exceptions.reservation;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ClusterInsufficientResourcesException extends ReservationCreationException {

    public ClusterInsufficientResourcesException() {
        super(I18n.CLUSTER_RESOURCES_INSUFFICIENT);
    }

    public ClusterInsufficientResourcesException(String message) {
        super(message);
    }

    public ClusterInsufficientResourcesException(Throwable cause) {
        super(I18n.CLUSTER_RESOURCES_INSUFFICIENT, cause);
    }

    public ClusterInsufficientResourcesException(String message, Throwable cause) {
        super(message, cause);
    }
}
