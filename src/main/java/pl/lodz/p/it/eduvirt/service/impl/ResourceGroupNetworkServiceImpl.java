package pl.lodz.p.it.eduvirt.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.*;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.NetworkInterfaceNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineConflictException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineNotFoundException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.ResourceGroupNetworkService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.priviliges.PrivilegesService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ResourceGroupNetworkServiceImpl implements ResourceGroupNetworkService {
    private final ResourceGroupNetworkRepository resourceGroupNetworkRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final VirtualMachineRepository virtualMachineRepository;
    private final OVirtVmService oVirtVmService;
    private final NetworkInterfaceRepository networkInterfaceRepository;
    private final CourseRepository courseRepository;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final CourseMetricRepository courseMetricRepository;

    private final EntityManager entityManager;
    private final ETagHelper eTagHelper;
    private final PrivilegesService privilegesService;

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public ResourceGroupNetwork addResourceGroupNetwork(UUID rgId, String name, String etag) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(rgId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(rgId));

        if (!privilegesService.validateResourceGroupOwnership(resourceGroup)) {
            throw new ResourceGroupNotFoundException(rgId);
        }

        if (!eTagHelper.validateEtag(etag, resourceGroup)) {
            throw new ResourceGroupConflictException();
        }

        if (resourceGroupNetworkRepository.existsByResourceGroupAndName(resourceGroup, name)) {
            throw new NetworkAlreadyExistsException();
        }

        Course course;

        if (resourceGroup.isStateless()) {
            course = resourceGroupPoolRepository.findByResourceGroupsContaining(resourceGroup).getCourse();
        } else {
            course = courseRepository.findByStateFulResourceGroupsContaining(resourceGroup);
        }


        Optional<CourseMetric> metric = courseMetricRepository.findByCourseIdAndMetricName(course.getId(), "network_count");
        if (metric.isPresent()) {
            var networkCount = resourceGroup.getNetworks().size();
            var metricValue = metric.get().getValue();

            if (metricValue != 0 && networkCount >= metricValue) {
                throw new NoNetworkAvailableException("Resource group has reached the maximum number of networks");
            }
        }

        ResourceGroupNetwork resourceGroupNetwork = new ResourceGroupNetwork();
        resourceGroupNetwork.setName(name);
        resourceGroupNetwork.setResourceGroup(resourceGroup);

        ResourceGroupNetwork save = resourceGroupNetworkRepository.save(resourceGroupNetwork);
        entityManager.lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        return save;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public List<ResourceGroupNetwork> getResourceGroupNetworks(UUID rgId) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(rgId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(rgId));

        if (!privilegesService.validateResourceGroupOwnershipOrAdmin(resourceGroup)) {
            throw new ResourceGroupNotFoundException(rgId);
        }

        return resourceGroupNetworkRepository.getAllByResourceGroupId(rgId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public void attachNicToNetwork(UUID networkId, UUID vmId, UUID nicId, String etag) {
        ResourceGroupNetwork resourceGroupNetwork = resourceGroupNetworkRepository
                .findById(networkId)
                .orElseThrow(() -> new ResourceGroupNetworkNotFoundException(networkId));

        ResourceGroup resourceGroup = resourceGroupNetwork.getResourceGroup();
        if (!privilegesService.validateResourceGroupOwnership(resourceGroup)) {
            throw new ResourceGroupNotFoundException(resourceGroup.getId());
        }

        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId)
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId));

        if (!eTagHelper.validateEtag(etag, virtualMachine.getId(), virtualMachine.getVersion())) {
            throw new VirtualMachineConflictException();
        }

        if (resourceGroupNetwork.getResourceGroup().getId() != virtualMachine.getResourceGroup().getId()) {
            throw new ResourceGroupNetworkNotFoundException(networkId);
        }

        boolean isNicFromVm = oVirtVmService.findNicsByVmId(vmId.toString())
                .stream()
                .anyMatch(nic -> Objects.equals(nic.id(), nicId.toString()));

        if (isNicFromVm) {
            NetworkInterface networkInterface = NetworkInterface.builder()
                    .id(nicId)
                    .virtualMachine(virtualMachine)
                    .resourceGroupNetwork(resourceGroupNetwork)
                    .build();
            resourceGroupNetwork.getInterfaces().add(networkInterface);
            resourceGroupNetworkRepository.save(resourceGroupNetwork);
            entityManager.lock(virtualMachine, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public void detachNicFromNetwork(UUID vmId, UUID nicId, String etag) {
        VirtualMachine virtualMachine = virtualMachineRepository.findById(vmId)
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId));
        ResourceGroup resourceGroup = virtualMachine.getResourceGroup();

        if (!privilegesService.validateResourceGroupOwnership(resourceGroup)) {
            throw new ResourceGroupNotFoundException(resourceGroup.getId());
        }

        if (!eTagHelper.validateEtag(etag, virtualMachine.getId(), virtualMachine.getVersion())) {
            throw new VirtualMachineConflictException();
        }

        NetworkInterface networkInterface = networkInterfaceRepository.findById(nicId)
                .orElseThrow(() -> new NetworkInterfaceNotFoundException(nicId));

        if (!networkInterface.getVirtualMachine().getId().equals(vmId)) {
            throw new NetworkInterfaceNotFoundException(nicId);
        }

        networkInterfaceRepository.deleteByIdAndVirtualMachine_Id(nicId, vmId);
        entityManager.lock(virtualMachine, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public void deleteNetwork(UUID networkId, UUID rgId, String etag) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(rgId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(rgId));

        if (!privilegesService.validateResourceGroupOwnership(resourceGroup)) {
            throw new ResourceGroupNotFoundException(rgId);
        }

        if (!eTagHelper.validateEtag(etag, resourceGroup)) {
            throw new ResourceGroupConflictException();
        }

        resourceGroupNetworkRepository.deleteById(networkId);
        entityManager.lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }
}