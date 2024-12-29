package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.NotFoundException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class VnicProfileEduvirtNotFoundException extends NotFoundException {

    public VnicProfileEduvirtNotFoundException(UUID vnicProfileId) {
        super("Vnic profile with id %s could not be found".formatted(vnicProfileId), I18n.VNIC_PROFILE_EDUVIRT_NOT_FOUND);
    }
}
