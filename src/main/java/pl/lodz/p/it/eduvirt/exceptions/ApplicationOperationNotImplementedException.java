package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.util.I18n;

public class ApplicationOperationNotImplementedException extends ApplicationBaseException {

    public ApplicationOperationNotImplementedException() {
        super(I18n.OPERATION_NOT_IMPLEMENTED);
    }

    public ApplicationOperationNotImplementedException(String message) {
        super(message);
    }

    public ApplicationOperationNotImplementedException(Throwable cause) {
        super(I18n.OPERATION_NOT_IMPLEMENTED, cause);
    }

    public ApplicationOperationNotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }
}
