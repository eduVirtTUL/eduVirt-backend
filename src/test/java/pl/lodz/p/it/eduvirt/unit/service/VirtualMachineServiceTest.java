package pl.lodz.p.it.eduvirt.unit.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Vm;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupConflictException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineClusterMismatchException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineConflictException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineNotFoundException;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.VirtualMachineRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.impl.VirtualMachineServiceImpl;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.priviliges.PrivilegesService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VirtualMachineServiceTest {

    @Mock
    private ResourceGroupRepository resourceGroupRepository;

    @Mock
    private PrivilegesService privilegesService;

    @Mock
    private ETagHelper eTagHelper;

    @Mock
    private CourseService courseService;

    @Mock
    private VirtualMachineRepository virtualMachineRepository;

    @Mock
    private OVirtVmService oVirtVmService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private VirtualMachineServiceImpl sut;

    @Test
    void Given_ResourceGroupDoNotExists_When_CreateVirtualMachine_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        boolean hidden = false;
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        Assertions.assertThrows(ResourceGroupNotFoundException.class, () -> sut.createVirtualMachine(rgId, id, hidden, ""));
    }

    @Test
    void Given_UserIsNotOwner_When_CreateVirtualMachine_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        boolean hidden = false;
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(false);
        // When
        // Then
        Assertions.assertThrows(ResourceGroupNotFoundException.class, () -> sut.createVirtualMachine(rgId, id, hidden, ""));
    }

    @Test
    void Given_ETagIsNotValid_When_CreateVirtualMachine_Then_ThrowResourceGroupConflictException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        boolean hidden = false;
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(false);
        // When
        // Then
        Assertions.assertThrows(ResourceGroupConflictException.class, () -> sut.createVirtualMachine(rgId, id, hidden, ""));
    }

    @Test
    void Given_VMAlreadyExists_When_CreateVirtualMachine_Then_ThrowVirtualMachineAlreadyExistsException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        boolean hidden = false;
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(virtualMachineRepository.existsById(any()))
                .thenReturn(true);
        // When
        // Then
        Assertions.assertThrows(VirtualMachineAlreadyExistsException.class, () -> sut.createVirtualMachine(rgId, id, hidden, ""));
    }

    @Test
    void Given_CourseAndVmClusterMismatch_When_CreateVirtualMachine_Then_ThrowVirtualMachineClusterMismatchException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID clusterID = UUID.randomUUID();
        boolean hidden = false;
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        Course course = Course.builder()
                .clusterId(clusterID)
                .build();
        Vm oVirtVm = mock(Vm.class);
        Cluster oVirtCluster = mock(Cluster.class);
        when(oVirtVm.cluster())
                .thenReturn(oVirtCluster);
        when(oVirtCluster.id())
                .thenReturn(UUID.randomUUID().toString());

        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(virtualMachineRepository.existsById(any()))
                .thenReturn(false);
        when(courseService.getCourseByResourceGroup(any()))
                .thenReturn(course);
        when(oVirtVmService.findVmById(any()))
                .thenReturn(oVirtVm);
        // When
        // Then
        Assertions.assertThrows(VirtualMachineClusterMismatchException.class, () -> sut.createVirtualMachine(rgId, id, hidden, ""));
    }

    @Test
    void Given_InputDataIsCorrect_When_CreateVirtualMachine_Then_CreateVirtualMachine() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID clusterID = UUID.randomUUID();
        boolean hidden = false;
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        Course course = Course.builder()
                .clusterId(clusterID)
                .build();
        Vm oVirtVm = mock(Vm.class);
        Cluster oVirtCluster = mock(Cluster.class);
        when(oVirtVm.cluster())
                .thenReturn(oVirtCluster);
        when(oVirtCluster.id())
                .thenReturn(clusterID.toString());

        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(virtualMachineRepository.existsById(any()))
                .thenReturn(false);
        when(courseService.getCourseByResourceGroup(any()))
                .thenReturn(course);
        when(oVirtVmService.findVmById(any()))
                .thenReturn(oVirtVm);
        // When
        sut.createVirtualMachine(rgId, id, hidden, "");
        // Then
        verify(virtualMachineRepository, times(1))
                .save(any());
        verify(entityManager, times(1))
                .lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Test
    void Given_ResourceGroupDoNotExists_When_DeleteVirtualMachine_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        Assertions.assertThrows(ResourceGroupNotFoundException.class, () -> sut.deleteVirtualMachine(id, rgId, ""));
    }

    @Test
    void Given_UserIsNotOwner_When_DeleteVirtualMachine_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(false);
        // When
        // Then
        Assertions.assertThrows(ResourceGroupNotFoundException.class, () -> sut.deleteVirtualMachine(id, rgId, ""));
    }

    @Test
    void Given_ETagIsNotValid_When_DeleteVirtualMachine_Then_ThrowResourceGroupConflictException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(false);
        // When
        // Then
        Assertions.assertThrows(ResourceGroupConflictException.class, () -> sut.deleteVirtualMachine(id, rgId, ""));
    }

    @Test
    void Given_VMDoNotExists_When_DeleteVirtualMachine_Then_ThrowVirtualMachineNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        Assertions.assertThrows(VirtualMachineNotFoundException.class, () -> sut.deleteVirtualMachine(id, rgId, ""));
    }

    @Test
    void Given_VirtualMachineIsNotFromResourceGroup_When_DeleteVirtualMachine_Then_ThrowVirtualMachineNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        // When
        // Then
        Assertions.assertThrows(VirtualMachineNotFoundException.class, () -> sut.deleteVirtualMachine(id, rgId, ""));
    }

    @Test
    void Given_InputDataIsCorrect_When_DeleteVirtualMachine_Then_DeleteVirtualMachine() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(resourceGroup)
                .build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        // When
        sut.deleteVirtualMachine(id, rgId, "");
        // Then
        verify(virtualMachineRepository, times(1))
                .delete(any());
        verify(entityManager, times(1))
                .lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Test
    void Given_VMDoNotExists_When_UpdateVirtualMachine_Then_ThrowVirtualMachineNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        Assertions.assertThrows(VirtualMachineNotFoundException.class, () -> sut.updateVirtualMachine(id, false, ""));
    }

    @Test
    void Given_UserIsNotOwner_When_UpdateVirtualMachine_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID id = UUID.randomUUID();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(false);
        // When
        // Then
        Assertions.assertThrows(ResourceGroupNotFoundException.class, () -> sut.updateVirtualMachine(id, false, ""));
    }

    @Test
    void Given_ETagIsNotValid_When_UpdateVirtualMachine_Then_ThrowVirtualMachineConflictException() {
        // Given
        UUID id = UUID.randomUUID();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(false);
        // When
        // Then
        Assertions.assertThrows(VirtualMachineConflictException.class, () -> sut.updateVirtualMachine(id, false, ""));
    }

    @Test
    void Given_InputDataIsCorrect_When_UpdateVirtualMachine_Then_UpdateVirtualMachine() {
        // Given
        UUID id = UUID.randomUUID();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(true);
        // When
        sut.updateVirtualMachine(id, false, "");
        // Then
        verify(virtualMachineRepository, times(1))
                .save(any());
    }
}
