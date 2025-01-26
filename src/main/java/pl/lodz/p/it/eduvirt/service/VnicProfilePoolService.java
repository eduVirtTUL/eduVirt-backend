package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VnicProfilePoolService {

    Map<Boolean, List<VnicProfile>> getSynchronizedVnicProfiles(Pageable pageable);

    List<VnicProfilePoolMember> getVnicProfilesPool();

    VnicProfilePoolMember getVnicProfileFromPool(UUID vnicProfileId);

    VnicProfilePoolMember addVnicProfileToPool(UUID vnicProfileId);

    void removeVnicProfileFromPool(UUID vnicProfileId);

    void markVnicProfileAsOccupied(UUID vnicProfileId);

    void markVnicProfileAsFree(UUID vnicProfileId);

}
