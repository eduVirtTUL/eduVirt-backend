package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.VirtualMachineService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VirtualMachineServiceImpl implements VirtualMachineService {
    private final VirtualMachineRepository virtualMachineRepository;
    private final ResourceGroupRepository resourceGroupRepository;

    @Override
    public void createVirtualMachine(UUID id, boolean hidden, ResourceGroup resourceGroup) {
        VirtualMachine vm = VirtualMachine.builder()
                .id(id)
                .hidden(hidden)
                .resourceGroup(resourceGroup)
                .build();

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

        virtualMachineRepository.delete(vm);
    }
}
