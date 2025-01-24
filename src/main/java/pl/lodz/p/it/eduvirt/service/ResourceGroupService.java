package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Vm;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDtoWthEtag;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;

import java.util.List;
import java.util.UUID;

public interface ResourceGroupService {
    List<ResourceGroup> getResourceGroups();

    List<VmDto> getVms(UUID id);

    VmDtoWthEtag getVm(UUID id);

    ResourceGroup getResourceGroup(UUID id);

    List<Vm> findAvailableVms(UUID rgId);

    void deleteResourceGroup(UUID id);

    ResourceGroup updateResourceGroup(UUID id, ResourceGroup resourceGroup, String etag);
}

