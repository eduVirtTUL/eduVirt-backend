package pl.lodz.p.it.eduvirt.exceptions.pod;

import pl.lodz.p.it.eduvirt.util.I18n;

public class PodNotFoundException extends PodBaseException {

    public PodNotFoundException() {
        super(I18n.POD_NOT_FOUND);
    }

    public PodNotFoundException(String message) {
        super(message);
    }

    public PodNotFoundException(Throwable cause) {
        super(I18n.POD_NOT_FOUND, cause);
    }

    public PodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
