package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.ApplicationBaseException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class VmOvirtOperationException extends ApplicationBaseException {

    public VmOvirtOperationException(String message) {
        super(message, I18n.VM_OVIRT_OPERATION_EXCEPTION);
    }

    public VmOvirtOperationException(String message, Throwable cause) {
        super(I18n.VM_OVIRT_OPERATION_EXCEPTION, message, cause);
    }
}
