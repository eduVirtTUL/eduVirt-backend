package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;

import java.util.List;
import java.util.UUID;

public interface ResourceGroupPoolService {
    ResourceGroupPool addResourceGroupPool(ResourceGroupPool resourceGroupPool, UUID courseId);

    Page<ResourceGroupPool> getResourceGroupPools(int page, int size);

    List<ResourceGroupPool> getResourceGroupPoolsByCourse(UUID courseId);

    ResourceGroupPool getResourceGroupPool(UUID id);

    void addResourceGroupToPool(UUID poolId, ResourceGroup resourceGroup);

    void deleteResourceGroupPool(UUID id);

    ResourceGroupPool updateResourceGroupPool(UUID id, ResourceGroupPool resourceGroupPool);
}
