package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.NicMapper;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupNetworkRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupRepository;
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
    private final ResourceGroupNetworkRepository resourceGroupNetworkRepository;


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
        return
                VmDto.builder()
                        .id(vm.id())
                        .name(vm.name())
                        .cpuCount(vm.cpu().topology().socketsAsInteger())
                        .memory(vm.memory().divide(BigInteger.valueOf(1024L * 1024L)).longValue())
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

                                    resourceGroupNetworkRepository
                                            .getNetworkByInterface(UUID.fromString(nic.id()))
                                            .ifPresent(resourceGroupNetwork -> nicDtoBuilder.segmentName(resourceGroupNetwork.getName()));

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
