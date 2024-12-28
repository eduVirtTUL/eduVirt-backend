package pl.lodz.p.it.eduvirt.exceptions.pod;

import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;

public class PodBaseException extends ApplicationBaseException {

    public PodBaseException() {
    }

    public PodBaseException(String message) {
        super(message);
    }

    public PodBaseException(Throwable cause) {
        super(cause);
    }

    public PodBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
