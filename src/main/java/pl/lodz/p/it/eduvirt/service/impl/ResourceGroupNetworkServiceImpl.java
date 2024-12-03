package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupNetwork;
import pl.lodz.p.it.eduvirt.entity.eduvirt.VirtualMachine;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupNetworkRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.ResourceGroupNetworkService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ResourceGroupNetworkServiceImpl implements ResourceGroupNetworkService {
    private final ResourceGroupNetworkRepository resourceGroupNetworkRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final VirtualMachineRepository virtualMachineRepository;

    @Override
    public ResourceGroupNetwork addResourceGroupNetwork(UUID rgId, String name) {
        ResourceGroupNetwork resourceGroupNetwork = new ResourceGroupNetwork();
        resourceGroupNetwork.setName(name);
        resourceGroupNetwork.setResourceGroup(resourceGroupRepository.getReferenceById(rgId));
        return resourceGroupNetworkRepository.save(resourceGroupNetwork);
    }

    @Override
    public List<ResourceGroupNetwork> getResourceGroupNetworks(UUID rgId) {
        return resourceGroupNetworkRepository.getAllByResourceGroupId(rgId);
    }

    @Override
    @Transactional
    public void attachVmToNetwork(UUID networkId, UUID vmId) {
        ResourceGroupNetwork resourceGroupNetwork = resourceGroupNetworkRepository.findById(networkId).orElseThrow();
        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow();

        resourceGroupNetwork.getVirtualMachines().add(virtualMachine);
        resourceGroupNetworkRepository.save(resourceGroupNetwork);
    }

    @Override
    @Transactional
    public void detachVmFromNetwork(UUID networkId, UUID vmId) {
        ResourceGroupNetwork resourceGroupNetwork = resourceGroupNetworkRepository.findById(networkId).orElseThrow();
        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow();

        resourceGroupNetwork.getVirtualMachines().remove(virtualMachine);
        resourceGroupNetworkRepository.save(resourceGroupNetwork);
    }
}