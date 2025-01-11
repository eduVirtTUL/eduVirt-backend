package pl.lodz.p.it.eduvirt.exceptions.general;

import pl.lodz.p.it.eduvirt.util.I18n;

public final class OperationNotImplementedException extends ApplicationBaseException {

    public OperationNotImplementedException() {
        super("Performed operation is yet to be implemented", I18n.OPERATION_NOT_IMPLEMENTED);
    }
}
