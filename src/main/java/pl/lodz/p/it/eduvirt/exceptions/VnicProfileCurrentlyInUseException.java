package pl.lodz.p.it.eduvirt.exceptions;

import pl.lodz.p.it.eduvirt.exceptions.general.BadRequestException;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.util.UUID;

public class VnicProfileCurrentlyInUseException extends BadRequestException {

    public VnicProfileCurrentlyInUseException(UUID vnicProfileId) {
        super("Vnic profile is currently in use, which prohibits performing operations on it",
                I18n.VNIC_PROFILE_CURRENTLY_IN_USE);
    }
}
