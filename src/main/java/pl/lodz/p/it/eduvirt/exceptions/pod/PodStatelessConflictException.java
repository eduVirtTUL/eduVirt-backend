package pl.lodz.p.it.eduvirt.exceptions.pod;

import pl.lodz.p.it.eduvirt.util.I18n;

public class PodStatelessConflictException extends PodBaseException {

    public PodStatelessConflictException() {
        super(I18n.POD_STATELESS_CONFLICT);
    }

    public PodStatelessConflictException(String message) {
        super(message);
    }

    public PodStatelessConflictException(Throwable cause) {
        super(I18n.POD_STATELESS_CONFLICT, cause);
    }

    public PodStatelessConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
