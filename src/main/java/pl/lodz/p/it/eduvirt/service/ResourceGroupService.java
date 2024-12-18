package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;

import java.util.List;
import java.util.UUID;

public interface ResourceGroupService {
    List<ResourceGroup> getResourceGroups();

    List<VmDto> getVms(UUID id);

    VmDto getVm(UUID id);

    ResourceGroup getResourceGroup(UUID id);

    ResourceGroup createResourceGroup(ResourceGroup resourceGroup);
}
