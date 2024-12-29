package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

public class VnicProfileOvirtNotFoundException extends NotFoundException {

    public VnicProfileOvirtNotFoundException(String message) {
        super(message, I18n.VNIC_PROFILE_OVIRT_NOT_FOUND);
    }
}
