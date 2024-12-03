package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.dto.vnic_profile.OvirtVnicProfileDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePoolMember;

import java.util.List;
import java.util.UUID;

/// todo think about splitting into OVirtVnicProfileService and EduVirtVnicProfileService
public interface OVirtVnicProfileService {

    List<OvirtVnicProfileDto> fetchVnicProfiles();

    List<VnicProfilePoolMember> getVnicProfilesPool();

    VnicProfilePoolMember addVnicProfileToPool(UUID vnicProfileId);

    void removeVnicProfileFromPool(UUID vnicProfileId);

    void markVnicProfileAsOccupied(UUID vnicProfileId);

    void markVnicProfileAsFree(UUID vnicProfileId);

    //todo add methods to handle CRUD for vlans range
}
