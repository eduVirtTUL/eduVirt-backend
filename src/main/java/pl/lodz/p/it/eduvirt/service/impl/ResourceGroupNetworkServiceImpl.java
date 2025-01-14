package pl.lodz.p.it.eduvirt.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.NoNetworkAvailableException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.ResourceGroupNetworkService;
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

    @Override
    @Transactional
    public ResourceGroupNetwork addResourceGroupNetwork(UUID rgId, String name, String etag) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(rgId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(rgId));

        if (!eTagHelper.validateEtag(etag, resourceGroup)) {
            throw new IllegalArgumentException("Resource group has been modified");
        }

        Course course;

        if (resourceGroup.isStateless()) {
            course = resourceGroupPoolRepository.findByResourceGroupsContaining(resourceGroup).getCourse();
        } else {
            course = courseRepository.findByStateFullResourceGroupsContaining(resourceGroup);
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
            NetworkInterface networkInterface = NetworkInterface.builder()
                    .id(nicId)
                    .virtualMachine(virtualMachine)
                    .resourceGroupNetwork(resourceGroupNetwork)
                    .build();
            resourceGroupNetwork.getInterfaces().add(networkInterface);
            resourceGroupNetworkRepository.save(resourceGroupNetwork);
        }
    }

    @Override
    @Transactional
    public void detachNicFromNetwork(UUID vmId, UUID nicId) {

        NetworkInterface networkInterface = networkInterfaceRepository.findById(nicId).orElseThrow();

        if (!networkInterface.getVirtualMachine().getId().equals(vmId)) {
            throw new IllegalArgumentException("Nic does not belong to the VM");
        }

        networkInterfaceRepository.deleteByIdAndVirtualMachine_Id(nicId, vmId);
    }

    @Override
    @Transactional
    public void deleteNetwork(UUID networkId, String etag) {
        ResourceGroupNetwork network = resourceGroupNetworkRepository.findById(networkId).orElseThrow();
        ResourceGroup rg = network.getResourceGroup();

        resourceGroupNetworkRepository.deleteById(networkId);
        entityManager.lock(rg, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }
}