package pl.lodz.p.it.eduvirt.exceptions.pod;

import pl.lodz.p.it.eduvirt.util.I18n;

public class PodAlreadyExistsException extends PodBaseException {

    public PodAlreadyExistsException() {
        super(I18n.POD_ALREADY_EXISTS);
    }

    public PodAlreadyExistsException(String message) {
        super(message);
    }

    public PodAlreadyExistsException(Throwable cause) {
        super(I18n.POD_ALREADY_EXISTS, cause);
    }

    public PodAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
