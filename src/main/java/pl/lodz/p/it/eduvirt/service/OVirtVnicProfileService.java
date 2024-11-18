package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.VnicProfile;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePoolMember;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/// todo think about splitting into OVirtVnicProfileService and EduVirtVnicProfileService
public interface OVirtVnicProfileService {

    Map<Boolean, List<VnicProfile>> getSynchronizedVnicProfiles();

    List<VnicProfile> fetchOVirtVnicProfiles();

    List<VnicProfilePoolMember> getVnicProfilesPool();

    VnicProfilePoolMember addVnicProfileToPool(UUID vnicProfileId);

    void removeVnicProfileFromPool(UUID vnicProfileId);

    void markVnicProfileAsOccupied(UUID vnicProfileId);

    void markVnicProfileAsFree(UUID vnicProfileId);

    //todo add methods to handle CRUD for vlans range
}
