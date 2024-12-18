package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupNetwork;
import pl.lodz.p.it.eduvirt.entity.eduvirt.VirtualMachine;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupNetworkRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.ResourceGroupNetworkService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ResourceGroupNetworkServiceImpl implements ResourceGroupNetworkService {
    private final ResourceGroupNetworkRepository resourceGroupNetworkRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final VirtualMachineRepository virtualMachineRepository;
    private final OVirtVmService oVirtVmService;

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
    public void attachNicToNetwork(UUID networkId, UUID vmId, UUID nicId) {
        ResourceGroupNetwork resourceGroupNetwork = resourceGroupNetworkRepository.findById(networkId).orElseThrow();

        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow();
        if (resourceGroupNetwork.getResourceGroup().getId() != virtualMachine.getResourceGroup().getId()) {
            throw new IllegalArgumentException("VM and network are not in the same resource group");
        }

        boolean isNicFromVm = oVirtVmService.findNicsByVmId(vmId.toString())
                .stream()
                .anyMatch(nic -> Objects.equals(nic.id(), nicId.toString()));

        if (isNicFromVm) {
            resourceGroupNetwork.getInterfaces().add(nicId);
            resourceGroupNetworkRepository.save(resourceGroupNetwork);
        }
    }

    @Override
    @Transactional
    public void detachNicFromNetwork(UUID vmId, UUID nicId) {
        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId).orElseThrow();
        ResourceGroupNetwork resourceGroupNetwork = resourceGroupNetworkRepository.findByResourceGroupIdAndInterfacesContains(
                virtualMachine.getResourceGroup().getId(), nicId
        ).orElseThrow();

        resourceGroupNetwork.getInterfaces().remove(nicId);
        resourceGroupNetworkRepository.save(resourceGroupNetwork);
    }

    @Override
    public void deleteNetwork(UUID networkId) {
        resourceGroupNetworkRepository.deleteById(networkId);
    }
}