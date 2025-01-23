package pl.lodz.p.it.eduvirt.exceptions.pod;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class PodDeletionException extends ApplicationBaseException {

    public PodDeletionException(String message) {
        super(message, I18n.POD_DELETION_EXCEPTION);
    }
}
