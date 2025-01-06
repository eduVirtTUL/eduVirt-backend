package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;

import java.util.List;
import java.util.UUID;

public interface ResourceGroupPoolService {
    ResourceGroupPool addResourceGroupPool(ResourceGroupPool resourceGroupPool, UUID courseId);

    List<ResourceGroupPool> getResourceGroupPools();

    List<ResourceGroupPool> getResourceGroupPoolsByCourse(UUID courseId);

    ResourceGroupPool getResourceGroupPool(UUID id);

    void addResourceGroupToPool(UUID poolId, ResourceGroup resourceGroup);

    void deleteResourceGroupPool(UUID id);

    ResourceGroupPool updateResourceGroupPool(UUID id, ResourceGroupPool resourceGroupPool);
}
