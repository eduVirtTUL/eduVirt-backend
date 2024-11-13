package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupPool;

import java.util.List;
import java.util.UUID;

public interface ResourceGroupPoolService {
    ResourceGroupPool addResourceGroupPool(ResourceGroupPool resourceGroupPool, UUID courseId);

    List<ResourceGroupPool> getResourceGroupPools();

    List<ResourceGroupPool> getResourceGroupPoolsByCourse(UUID courseId);
}
