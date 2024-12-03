package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.VnicProfile;

public interface OVirtVnicProfileService {

    VnicProfile getVnicProfileById(String vnicProfileId);
}
