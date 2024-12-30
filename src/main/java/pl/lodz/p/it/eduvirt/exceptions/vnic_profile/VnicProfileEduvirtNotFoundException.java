package pl.lodz.p.it.eduvirt.exceptions.vnic_profile;

import pl.lodz.p.it.eduvirt.exceptions.NotFoundException;

import java.util.UUID;

public class VnicProfileEduvirtNotFoundException extends NotFoundException {
    public VnicProfileEduvirtNotFoundException(String message) {
        super(message);
    }

    public VnicProfileEduvirtNotFoundException(UUID vnicProfileId) {
        super("Not found in EduVirt the vnic profile with id:" + vnicProfileId);
    }
}
