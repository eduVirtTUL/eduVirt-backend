package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDtoWthEtag;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupAlreadyExists;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupConflictException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.NicMapper;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.service.priviliges.PrivilegesService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class ResourceGroupServiceImpl implements ResourceGroupService {
    private final ResourceGroupRepository resourceGroupRepository;
    private final OVirtVmService oVirtVmService;
    private final NicMapper nicMapper;
    private final OVirtVnicProfileService oVirtVnicProfileService;
    private final VirtualMachineRepository virtualMachineRepository;
    private final NetworkInterfaceRepository networkInterfaceRepository;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final PodStatefulRepository podStatefulRepository;
    private final CourseRepository courseRepository;
    private final ETagHelper eTagHelper;
    private final PrivilegesService privilegesService;

    @Override
    @Transactional
    public List<ResourceGroup> getResourceGroups() {
        return resourceGroupRepository.findAll();
    }

    @Override
    public List<VmDto> getVms(UUID id) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceGroupNotFoundException(id));
        // TODO: Add access for student
        // validateOwnershipOrAdmin(resourceGroup);
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
    @Transactional
    public VmDtoWthEtag getVm(UUID id) {
        Vm vm = oVirtVmService.findVmById(id.toString());
        VirtualMachine vmEntity = virtualMachineRepository.findById(id).orElseThrow();
        ResourceGroup resourceGroup = vmEntity.getResourceGroup();
        if (!privilegesService.validateResourceGroupOwnershipOrAdmin(resourceGroup)) {
            throw new ResourceGroupNotFoundException(resourceGroup.getId());
        }

        String etag = eTagHelper.generateEtag(vmEntity.getId(), vmEntity.getVersion());

        return
                new VmDtoWthEtag(
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
                                .build(), etag);
    }


    @Override
    @Transactional
    public ResourceGroup getResourceGroup(UUID id) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(id).orElseThrow(() -> new ResourceGroupNotFoundException(id));
        // TODO: Return here
        // validateOwnershipOrAdmin(resourceGroup);
        return resourceGroup;
    }

    @Transactional
    @Override
    public List<Vm> findAvailableVms(UUID rgId) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(rgId).orElseThrow(() -> new ResourceGroupNotFoundException(rgId));

        if (!privilegesService.validateResourceGroupOwnership(resourceGroup)) {
            throw new ResourceGroupNotFoundException(rgId);
        }

        UUID clusterId;

        if (resourceGroup.isStateless()) {
            clusterId = resourceGroupPoolRepository.findByResourceGroupsContaining(resourceGroup).getCourse().getClusterId();
        } else {
            clusterId = courseRepository.findByStateFulResourceGroupsContaining(resourceGroup).getClusterId();
        }

        return oVirtVmService.findVms().stream()
                .filter(vm -> UUID.fromString(vm.cluster().id()).equals(clusterId))
                .toList();
    }

    @Transactional
    @Override
    public void deleteResourceGroup(UUID id) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(id).orElseThrow(() -> new ResourceGroupNotFoundException(id));
        boolean isOwner = privilegesService.validateResourceGroupOwnership(resourceGroup);
        if (!isOwner) {
            throw new ResourceGroupNotFoundException(id);
        }

        if (!resourceGroup.isStateless()) {
            Course course = courseRepository.findByStateFulResourceGroupsContaining(resourceGroup);
            course.getStateFulResourceGroups().remove(resourceGroup);
            courseRepository.save(course);
        } else {
            resourceGroupRepository.deleteById(id);
        }

    }

    @Transactional
    @Override
    public ResourceGroup updateResourceGroup(UUID id, ResourceGroup resourceGroup, String etag) {
        ResourceGroup existingResourceGroup = resourceGroupRepository.findById(id).orElseThrow(() -> new ResourceGroupNotFoundException(id));

        if (!privilegesService.validateResourceGroupOwnership(existingResourceGroup)) {
            throw new ResourceGroupNotFoundException(id);
        }

        if (!eTagHelper.validateEtag(etag, existingResourceGroup)) {
            throw new ResourceGroupConflictException();
        }


        boolean isNameTaken;
        if (existingResourceGroup.isStateless()) {
            ResourceGroupPool pool = resourceGroupPoolRepository.findByResourceGroupsContaining(existingResourceGroup);
            isNameTaken = pool.getResourceGroups().stream().anyMatch(rg ->
                    Objects.equals(rg.getName(), resourceGroup.getName()) &&
                            !Objects.equals(rg.getId(), existingResourceGroup.getId()));
        } else {
            existingResourceGroup.setDescription(resourceGroup.getDescription());
            existingResourceGroup.setMaxRentTime(resourceGroup.getMaxRentTime());

            Course course = courseRepository.findByStateFulResourceGroupsContaining(existingResourceGroup);
            isNameTaken = course.getStateFulResourceGroups().stream().anyMatch(rg ->
                    Objects.equals(rg.getName(), resourceGroup.getName())
                            && !Objects.equals(rg.getId(), existingResourceGroup.getId()));
        }

        if (isNameTaken) {
            throw new ResourceGroupAlreadyExists();
        }

        existingResourceGroup.setName(resourceGroup.getName());

        return resourceGroupRepository.save(existingResourceGroup);
    }
}
