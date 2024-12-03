package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;

import java.util.UUID;

public interface VirtualMachineService {
    void createVirtualMachine(UUID id, ResourceGroup resourceGroup);
}
