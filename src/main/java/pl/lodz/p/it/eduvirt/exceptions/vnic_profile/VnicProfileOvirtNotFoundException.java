package pl.lodz.p.it.eduvirt.exceptions.vnic_profile;

import pl.lodz.p.it.eduvirt.exceptions.NotFoundException;

import java.util.UUID;

public class VnicProfileOvirtNotFoundException extends NotFoundException {
    public VnicProfileOvirtNotFoundException(String message) {
        super(message);
    }

    public VnicProfileOvirtNotFoundException(UUID vnicProfileId) {
        super("Not found in EduVirt the vnic profile with id:" + vnicProfileId);
    }
}
