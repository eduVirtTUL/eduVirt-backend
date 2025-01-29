package pl.lodz.p.it.eduvirt.service.ovirt;

import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.network.VlansRange;

import java.util.List;

public interface OVirtVnicProfileService {

    VnicProfile getVnicProfileById(String vnicProfileId);

    List<VnicProfile> getVnicProfiles();

    List<VnicProfile> getVnicProfiles(Pageable pageable, VlansRange... vlansRanges);
}
