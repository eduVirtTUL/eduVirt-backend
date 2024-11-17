package pl.lodz.p.it.eduvirt.exceptions.cluster;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ClusterNotFoundException extends ClusterBaseException {

    public ClusterNotFoundException() {
        super(I18n.CLUSTER_NOT_FOUND);
    }

    public ClusterNotFoundException(String message) {
        super(message);
    }

    public ClusterNotFoundException(Throwable cause) {
        super(I18n.CLUSTER_NOT_FOUND, cause);
    }

    public ClusterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
