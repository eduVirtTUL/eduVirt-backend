package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;

import java.util.List;
import java.util.UUID;

public interface ResourceGroupService {
    List<ResourceGroup> getResourceGroups();

    ResourceGroup getResourceGroup(UUID id);

    ResourceGroup createResourceGroup(ResourceGroup resourceGroup);
}
