package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.dto.VnicProfileDto;
import pl.lodz.p.it.eduvirt.dto.VnicProfilePoolDto;

import java.util.List;
import java.util.UUID;

public interface OVirtVnicProfileService {

    List<VnicProfileDto> fetchVnicProfiles();

    List<VnicProfilePoolDto> showVnicProfilePool();

    boolean expandVnicProfilePool(UUID vnicProfileId);
}
