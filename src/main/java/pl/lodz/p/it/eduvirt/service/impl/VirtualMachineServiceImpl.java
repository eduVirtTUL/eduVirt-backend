package pl.lodz.p.it.eduvirt.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Vm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupConflictException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineClusterMismatchException;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.VirtualMachineService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VirtualMachineServiceImpl implements VirtualMachineService {
    private final VirtualMachineRepository virtualMachineRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final OVirtVmService oVirtVmService;

    private final EntityManager entityManager;
    private final ETagHelper eTagHelper;
    private final CourseService courseService;

    @Override
    @Transactional
    public void createVirtualMachine(UUID rgId, UUID id, boolean hidden, String etag) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(rgId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(rgId));

        if (!eTagHelper.validateEtag(etag, resourceGroup)) {
            throw new ResourceGroupConflictException();
        }

        if (virtualMachineRepository.existsById(id)) {
            throw new VirtualMachineAlreadyExistsException(id);
        }

        Course course = courseService.getCourseByResourceGroup(resourceGroup);

        Vm oVirtVm = oVirtVmService.findVmById(id.toString());

        if (!Objects.equals(oVirtVm.cluster().id(), course.getClusterId().toString())) {
            throw new VirtualMachineClusterMismatchException(id);
        }

        VirtualMachine vm = VirtualMachine.builder()
                .id(id)
                .hidden(hidden)
                .resourceGroup(resourceGroup)
                .build();

        virtualMachineRepository.save(vm);
        entityManager.lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Transactional
    @Override
    public void deleteVirtualMachine(UUID id, UUID rgId, String etag) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(rgId).orElseThrow();

        if (!eTagHelper.validateEtag(etag, resourceGroup)) {
            throw new ResourceGroupConflictException();
        }


        VirtualMachine vm = virtualMachineRepository.findById(id).orElseThrow();
        if (!vm.getResourceGroup().equals(resourceGroup)) {
            throw new IllegalArgumentException("Virtual machine does not belong to the resource group");
        }

        virtualMachineRepository.delete(vm);
        entityManager.lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Override
    @Transactional
    public void updateVirtualMachine(UUID id, boolean hidden) {
        VirtualMachine vm = virtualMachineRepository.findById(id).orElseThrow();
        vm.setHidden(hidden);
        virtualMachineRepository.save(vm);
    }
}
