package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VnicProfilePoolService {

    VnicProfilesAggregate getSynchronizedVnicProfiles(Pageable pageable);

    List<VnicProfilePoolMember> getVnicProfilesPool(Pageable pageable);

    VnicProfilePoolMember getVnicProfileFromPool(UUID vnicProfileId);

    Optional<VnicProfilePoolMember> getFirstFreeVnicProfileFromPool();

    VnicProfilePoolMember addVnicProfileToPool(UUID vnicProfileId);

    void removeVnicProfileFromPool(UUID vnicProfileId);

    void markVnicProfileAsOccupied(UUID vnicProfileId);

    void markVnicProfileAsFree(UUID vnicProfileId);

    record VnicProfilesAggregate(List<VnicProfilePoolMember> inPool, List<VnicProfile> outOfPool) {}
}
