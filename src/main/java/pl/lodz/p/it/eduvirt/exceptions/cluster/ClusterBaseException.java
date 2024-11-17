package pl.lodz.p.it.eduvirt.exceptions.cluster;

import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

public class ClusterBaseException extends ApplicationBaseException {

    public ClusterBaseException() {
    }

    public ClusterBaseException(String message) {
        super(message);
    }

    public ClusterBaseException(Throwable cause) {
        super(cause);
    }

    public ClusterBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
