package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceGroupServiceImpl implements ResourceGroupService {
    private final ResourceGroupRepository resourceGroupRepository;

    @Override
    public List<ResourceGroup> getResourceGroups() {
        return resourceGroupRepository.findAll();
    }

    @Override
    public ResourceGroup getResourceGroup(UUID id) {
        return resourceGroupRepository.findById(id).orElseThrow(() -> new ResourceGroupNotFoundException(id));
    }

    @Override
    public ResourceGroup createResourceGroup(ResourceGroup resourceGroup) {
        return resourceGroupRepository.save(resourceGroup);
    }
}
