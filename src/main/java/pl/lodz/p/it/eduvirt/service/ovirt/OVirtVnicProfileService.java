package pl.lodz.p.it.eduvirt.service.ovirt;

import org.ovirt.engine.sdk4.types.VnicProfile;

import java.util.List;

public interface OVirtVnicProfileService {

    VnicProfile getVnicProfileById(String vnicProfileId);

    List<VnicProfile> getVnicProfiles();
}
