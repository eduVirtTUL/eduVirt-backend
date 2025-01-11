package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class VnicProfileEduvirtNotFoundException extends NotFoundException {

    public VnicProfileEduvirtNotFoundException(String message) {
        super(message, I18n.VNIC_PROFILE_EDUVIRT_NOT_FOUND);
    }

    public VnicProfileEduvirtNotFoundException(UUID vnicProfileId) {
        super("Not found in EduVirt the vnic profile with id:" + vnicProfileId, I18n.VNIC_PROFILE_EDUVIRT_NOT_FOUND);
    }
}
