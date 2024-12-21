package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupNetwork;
import pl.lodz.p.it.eduvirt.entity.eduvirt.VirtualMachine;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupNetworkRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.VirtualMachineService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VirtualMachineServiceImpl implements VirtualMachineService {
    private final VirtualMachineRepository virtualMachineRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final OVirtVmService oVirtVmService;
    private final ResourceGroupNetworkRepository resourceGroupNetworkRepository;

    @Override
    public void createVirtualMachine(UUID id, boolean hidden, ResourceGroup resourceGroup) {
        VirtualMachine vm = new VirtualMachine(id, hidden, resourceGroup);
        virtualMachineRepository.save(vm);
    }

    @Transactional
    @Override
    public void deleteVirtualMachine(UUID id, UUID rgId) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(rgId).orElseThrow();
        VirtualMachine vm = virtualMachineRepository.findById(id).orElseThrow();
        if (!vm.getResourceGroup().equals(resourceGroup)) {
            throw new IllegalArgumentException("Virtual machine does not belong to the resource group");
        }

        List<UUID> nics = oVirtVmService.findVmById(id.toString())
                .nics()
                .stream()
                .map(nic -> UUID.fromString(nic.id()))
                .toList();

        List<ResourceGroupNetwork> networks = resourceGroup.getNetworks();
        for (UUID nic : nics) {
            networks
                    .forEach(net -> net.getInterfaces().remove(nic));
        }
        resourceGroupNetworkRepository.saveAll(networks);
        virtualMachineRepository.delete(vm);
    }
}
