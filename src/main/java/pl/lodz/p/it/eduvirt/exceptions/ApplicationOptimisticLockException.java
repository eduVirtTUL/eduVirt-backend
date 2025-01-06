package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class ApplicationOptimisticLockException extends ConflictException {

    public ApplicationOptimisticLockException(String message) {
        super(message, I18n.OPTIMISTIC_LOCK_EXCEPTION);
    }
}
