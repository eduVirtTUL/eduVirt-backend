package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class VmNotFoundException extends NotFoundException {

    public VmNotFoundException(String message) {
        super(message, I18n.VM_NOT_FOUND);
    }
}
