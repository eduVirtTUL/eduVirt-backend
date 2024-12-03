package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.eduvirt.VirtualMachine;
import pl.lodz.p.it.eduvirt.repository.eduvirt.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.VirtualMachineService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VirtualMachineServiceImpl implements VirtualMachineService {
    private final VirtualMachineRepository virtualMachineRepository;

    @Override
    public void createVirtualMachine(UUID id, ResourceGroup resourceGroup) {
        VirtualMachine vm = new VirtualMachine(id, resourceGroup);
        virtualMachineRepository.save(vm);
    }
}
