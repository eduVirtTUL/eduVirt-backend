package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class VnicProfileExistsException extends AlreadyExistsException {

    public VnicProfileExistsException(UUID vnicProfileId) {
        super("Vnic profile with id %s already exists", I18n.VNIC_PROFILE_ALREADY_EXISTS);
    }
}
