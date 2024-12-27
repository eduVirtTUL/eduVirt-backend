package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.NicMapper;
import pl.lodz.p.it.eduvirt.repository.NetworkInterfaceRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceGroupServiceImpl implements ResourceGroupService {
    private final ResourceGroupRepository resourceGroupRepository;
    private final OVirtVmService oVirtVmService;
    private final NicMapper nicMapper;
    private final OVirtVnicProfileService oVirtVnicProfileService;
    private final VirtualMachineRepository virtualMachineRepository;
    private final NetworkInterfaceRepository networkInterfaceRepository;


    @Override
    public List<ResourceGroup> getResourceGroups() {
        return resourceGroupRepository.findAll();
    }

    @Override
    public List<VmDto> getVms(UUID id) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceGroupNotFoundException(id));
        return resourceGroup.getVms()
                .parallelStream()
                .map(machine -> {
                    Vm vm = oVirtVmService.findVmById(machine.getId().toString());
                    return VmDto.builder()
                            .id(vm.id())
                            .name(vm.name())
                            .hidden(machine.isHidden())
                            .cpuCount(vm.cpu().topology().socketsAsInteger())
                            .memory(vm.memory().divide(BigInteger.valueOf(1024L * 1024L)).longValue())
                            .nics(nicMapper.nicsToDtos(vm.nics().stream()))
                            .build();
                })
                .toList();
    }

    @Override
    public VmDto getVm(UUID id) {
        Vm vm = oVirtVmService.findVmById(id.toString());
        VirtualMachine vmEntity = virtualMachineRepository.findById(id).orElseThrow();
        return
                VmDto.builder()
                        .id(vm.id())
                        .name(vm.name())
                        .cpuCount(vm.cpu().topology().socketsAsInteger())
                        .memory(vm.memory().divide(BigInteger.valueOf(1024L * 1024L)).longValue())
                        .hidden(vmEntity.isHidden())
                        .nics(
                                vm.nics().parallelStream().map(nic -> {
                                    NicDto.NicDtoBuilder nicDtoBuilder = NicDto.builder()
                                            .id(nic.id())
                                            .name(nic.name())
                                            .macAddress(nic.mac().address());

                                    if (nic.vnicProfilePresent()) {
                                        VnicProfile profile = oVirtVnicProfileService.getVnicProfileById(nic.vnicProfile().id());
                                        nicDtoBuilder
                                                .profileName(profile.name());
                                    }

                                    networkInterfaceRepository.findById(UUID.fromString(nic.id()))
                                            .ifPresent(networkInterface
                                                    -> nicDtoBuilder.segmentName(networkInterface.getResourceGroupNetwork().getName()));

                                    return nicDtoBuilder
                                            .build();

                                }).toList()
                        )
                        .build();
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
