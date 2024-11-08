package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.dto.VnicProfileDto;

import java.util.List;

public interface OVirtVnicProfileService {

    List<VnicProfileDto> fetchVnicProfiles();
}
