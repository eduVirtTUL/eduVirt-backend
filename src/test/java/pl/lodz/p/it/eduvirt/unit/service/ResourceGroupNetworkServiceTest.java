package pl.lodz.p.it.eduvirt.unit.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.types.Nic;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.*;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.NetworkInterfaceNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineConflictException;
import pl.lodz.p.it.eduvirt.exceptions.virtual_machine.VirtualMachineNotFoundException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.impl.ResourceGroupNetworkServiceImpl;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.priviliges.PrivilegesService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResourceGroupNetworkServiceTest {
    @Mock
    private ResourceGroupRepository resourceGroupRepository;

    @Mock
    private PrivilegesService privilegesService;

    @Mock
    private ETagHelper eTagHelper;

    @Mock
    private ResourceGroupNetworkRepository resourceGroupNetworkRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ResourceGroupPoolRepository resourceGroupPoolRepository;

    @Mock
    private CourseMetricRepository courseMetricRepository;

    @Mock
    private VirtualMachineRepository virtualMachineRepository;

    @Mock
    private NetworkInterfaceRepository networkInterfaceRepository;

    @Mock
    private OVirtVmService oVirtVmService;

    @InjectMocks
    private ResourceGroupNetworkServiceImpl sut;

    @Test
    void Given_ResourceGroupDoNotExists_When_AddResourceGroupNetwork_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(ResourceGroupNotFoundException.class, () -> sut.addResourceGroupNetwork(rgId, name, null));
    }

    @Test
    void Given_UserIsNotOwner_When_AddResourceGroupNetwork_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(new ResourceGroup()));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(ResourceGroupNotFoundException.class, () -> sut.addResourceGroupNetwork(rgId, name, null));
    }

    @Test
    void Given_EtagIsInvalid_When_AddResourceGroupNetwork_Then_ThrowResourceGroupConflictException() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(new ResourceGroup()));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(ResourceGroupConflictException.class, () -> sut.addResourceGroupNetwork(rgId, name, null));
    }

    @Test
    void Given_ResourceGroupNetworkAlreadyExists_When_AddResourceGroupNetwork_Then_ThrowNetworkAlreadyExistsException() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(new ResourceGroup()));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(resourceGroupNetworkRepository.existsByResourceGroupAndName(any(), any()))
                .thenReturn(true);
        // When
        // Then
        assertThrows(NetworkAlreadyExistsException.class, () -> sut.addResourceGroupNetwork(rgId, name, null));
    }

    @Test
    void Given_InputDataIsCorrectAndResourceGroupIsStateful_When_AddResourceGroupNetwork_Then_CreateResourceGroupNetwork() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        Course course = Course.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(resourceGroupNetworkRepository.existsByResourceGroupAndName(any(), any()))
                .thenReturn(false);
        when(courseRepository.findByStateFulResourceGroupsContaining(any()))
                .thenReturn(course);
        when(courseMetricRepository.findByCourseIdAndMetricName(any(), any()))
                .thenReturn(Optional.empty());
        // When
        sut.addResourceGroupNetwork(rgId, name, null);
        // Then
        verify(resourceGroupNetworkRepository, times(1))
                .save(any());
        verify(entityManager, times(1))
                .lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Test
    void Given_NetworkCountMetricValueExistsAndValueIdGrater_When_AddResourceGroupNetwork_Then_ThrowNoNetworkAvailableException() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .networks(List.of(
                        ResourceGroupNetwork.builder().build(),
                        ResourceGroupNetwork.builder().build()
                ))
                .build();
        Course course = Course.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(resourceGroupNetworkRepository.existsByResourceGroupAndName(any(), any()))
                .thenReturn(false);
        when(courseRepository.findByStateFulResourceGroupsContaining(any()))
                .thenReturn(course);
        when(courseMetricRepository.findByCourseIdAndMetricName(any(), any()))
                .thenReturn(Optional.of(CourseMetric.builder()
                        .value(1)
                        .build()));
        // When
        // Then
        assertThrows(NoNetworkAvailableException.class, () -> sut.addResourceGroupNetwork(rgId, name, null));
    }

    @Test
    void Given_NetworkCountMetricValueExistsAndValueIdLesser_When_AddResourceGroupNetwork_Then_CreateResourceGroupNetwork() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .networks(List.of(
                        ResourceGroupNetwork.builder().build()
                ))
                .build();
        Course course = Course.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(resourceGroupNetworkRepository.existsByResourceGroupAndName(any(), any()))
                .thenReturn(false);
        when(courseRepository.findByStateFulResourceGroupsContaining(any()))
                .thenReturn(course);
        when(courseMetricRepository.findByCourseIdAndMetricName(any(), any()))
                .thenReturn(Optional.of(CourseMetric.builder()
                        .value(3)
                        .build()));
        // When
        sut.addResourceGroupNetwork(rgId, name, null);
        // Then
        verify(resourceGroupNetworkRepository, times(1))
                .save(any());
        verify(entityManager, times(1))
                .lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Test
    void Given_NetworkCountMetricExistsAndValueIsZero_When_AddResourceGroupNetwork_Then_CreateResourceGroupNetwork() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .networks(List.of(
                        ResourceGroupNetwork.builder().build()
                ))
                .build();
        Course course = Course.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(resourceGroupNetworkRepository.existsByResourceGroupAndName(any(), any()))
                .thenReturn(false);
        when(courseRepository.findByStateFulResourceGroupsContaining(any()))
                .thenReturn(course);
        when(courseMetricRepository.findByCourseIdAndMetricName(any(), any()))
                .thenReturn(Optional.of(CourseMetric.builder()
                        .value(0)
                        .build()));
        // When
        sut.addResourceGroupNetwork(rgId, name, null);
        // Then
        verify(resourceGroupNetworkRepository, times(1))
                .save(any());
        verify(entityManager, times(1))
                .lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Test
    void Given_InputDataIsCorrectAndResourceGroupIsStateless_When_AddResourceGroupNetwork_Then_CreateResourceGroupNetwork() {
        // Given
        UUID rgId = UUID.randomUUID();
        String name = "name";
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .stateless(true)
                .build();
        Course course = Course.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        when(resourceGroupNetworkRepository.existsByResourceGroupAndName(any(), any()))
                .thenReturn(false);
        when(resourceGroupPoolRepository.findByResourceGroupsContaining(any()))
                .thenReturn(ResourceGroupPool.builder()
                        .course(course)
                        .build());
        when(courseMetricRepository.findByCourseIdAndMetricName(any(), any()))
                .thenReturn(Optional.empty());
        // When
        sut.addResourceGroupNetwork(rgId, name, null);
        // Then
        verify(resourceGroupNetworkRepository, times(1))
                .save(any());
        verify(entityManager, times(1))
                .lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Test
    void Given_UserIsNotOwnerOrAdmin_When_GetResourceGroupNetworks_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnershipOrAdmin(any()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(ResourceGroupNotFoundException.class, () -> sut.getResourceGroupNetworks(rgId));
    }

    @Test
    void Given_ResourceGroupDoNotExists_When_GetResourceGroupNetworks_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(ResourceGroupNotFoundException.class, () -> sut.getResourceGroupNetworks(rgId));
    }

    @Test
    void Given_UserIsOwnerOrAdmin_When_GetResourceGroupNetworks_Then_ReturnResourceGroupNetworks() {
        // Given
        UUID rgId = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnershipOrAdmin(any()))
                .thenReturn(true);
        // When
        sut.getResourceGroupNetworks(rgId);
        // Then
        verify(resourceGroupNetworkRepository, times(1))
                .getAllByResourceGroupId(rgId);
    }

    @Test
    void Given_ResourceGroupDoNotExists_When_DeleteNetwork_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID networkId = UUID.randomUUID();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(ResourceGroupNotFoundException.class, () -> sut.deleteNetwork(rgId, networkId, null));
    }

    @Test
    void Given_UserIdNotOwner_When_DeleteNetwork_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID networkId = UUID.randomUUID();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(new ResourceGroup()));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(ResourceGroupNotFoundException.class, () -> sut.deleteNetwork(rgId, networkId, null));
    }

    @Test
    void Given_ETagIsNotValid_When_DeleteNetwork_Then_ThrowResourceGroupConflictException() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID networkId = UUID.randomUUID();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(new ResourceGroup()));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(ResourceGroupConflictException.class, () -> sut.deleteNetwork(rgId, networkId, null));
    }

    @Test
    void Given_InputDataIsCorrect_When_DeleteNetwork_Then_DeleteNetwork() {
        // Given
        UUID rgId = UUID.randomUUID();
        UUID networkId = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroup));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any()))
                .thenReturn(true);
        // When
        sut.deleteNetwork(networkId, rgId, null);
        // Then
        verify(resourceGroupNetworkRepository, times(1))
                .deleteById(networkId);
        verify(entityManager, times(1))
                .lock(resourceGroup, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Test
    void Given_VirtualMachineDoNotExists_When_DetachNicFromNetwork_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(VirtualMachineNotFoundException.class, () -> sut.detachNicFromNetwork(vmId, nicId, null));
    }

    @Test
    void Given_EtagIsInvalid_When_DetachNicFromNetwork_Then_ThrowResourceGroupConflictException() {
        // Given
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .version(1)
                .id(vmId)
                .build();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(VirtualMachineConflictException.class, () -> sut.detachNicFromNetwork(vmId, nicId, null));
    }

    @Test
    void Given_UserIsNotOwner_When_DetachNicFromNetwork_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(ResourceGroupNotFoundException.class, () -> sut.detachNicFromNetwork(vmId, nicId, null));
    }

    @Test
    void Given_NetworkInterfaceDoNotExists_When_DetachNicFromNetwork_Then_ThrowIllegalArgumentException() {
        // Given
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(true);
        when(networkInterfaceRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(NetworkInterfaceNotFoundException.class, () -> sut.detachNicFromNetwork(vmId, nicId, null));
    }

    @Test
    void Given_NetworkInterfaceIsNotFromVm_When_DetachNicFromNetwork_Then_DoNothing() {
        // Given
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .id(vmId)
                .build();
        NetworkInterface networkInterface = NetworkInterface.builder()
                .virtualMachine(VirtualMachine.builder()
                        .id(UUID.randomUUID())
                        .build())
                .build();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(true);
        when(networkInterfaceRepository.findById(any()))
                .thenReturn(Optional.of(networkInterface));
        // When
        // Then
        assertThrows(NetworkInterfaceNotFoundException.class, () -> sut.detachNicFromNetwork(vmId, nicId, null));
        verify(resourceGroupNetworkRepository, never())
                .save(any());
    }

    @Test
    void Given_InputDataIsCorrect_When_DetachNicFromNetwork_Then_DeleteNetworkInterface() {
        // Given
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .id(vmId)
                .build();
        NetworkInterface networkInterface = NetworkInterface.builder()
                .virtualMachine(virtualMachine)
                .build();
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(true);
        when(networkInterfaceRepository.findById(any()))
                .thenReturn(Optional.of(networkInterface));
        // When
        sut.detachNicFromNetwork(vmId, nicId, null);
        // Then
        verify(networkInterfaceRepository, times(1))
                .deleteByIdAndVirtualMachine_Id(nicId, vmId);
        verify(entityManager, times(1))
                .lock(virtualMachine, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @Test
    void Given_NetworkDoNotExists_When_AttachNic_Then_ThrowResourceGroupNetworkNotFoundException() {
        // Given
        UUID networkId = UUID.randomUUID();
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        when(resourceGroupNetworkRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(ResourceGroupNetworkNotFoundException.class, () -> sut.attachNicToNetwork(networkId, vmId, nicId, ""));
    }

    @Test
    void Given_UserIsNotOwner_When_AttachNic_Then_ThrowResourceGroupNotFoundException() {
        // Given
        UUID networkId = UUID.randomUUID();
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        ResourceGroupNetwork resourceGroupNetwork = ResourceGroupNetwork.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(resourceGroupNetworkRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroupNetwork));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(ResourceGroupNotFoundException.class, () -> sut.attachNicToNetwork(networkId, vmId, nicId, ""));
    }

    @Test
    void Given_VirtualMachineDoNotExists_When_AttachNic_Then_ThrowVirtualMachineNotFoundException() {
        // Given
        UUID networkId = UUID.randomUUID();
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        ResourceGroupNetwork resourceGroupNetwork = ResourceGroupNetwork.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(resourceGroupNetworkRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroupNetwork));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(VirtualMachineNotFoundException.class, () -> sut.attachNicToNetwork(networkId, vmId, nicId, ""));
    }

    @Test
    void Given_EtagIsNotValid_When_AttachNic_Then_ThrowResourceGroupConflictException() {
        // Given
        UUID networkId = UUID.randomUUID();
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        ResourceGroupNetwork resourceGroupNetwork = ResourceGroupNetwork.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(ResourceGroup.builder().build())
                .build();
        when(resourceGroupNetworkRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroupNetwork));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(false);
        // When
        // Then
        assertThrows(VirtualMachineConflictException.class, () -> sut.attachNicToNetwork(networkId, vmId, nicId, ""));
    }

    @Test
    void Given_VmAndNetworkAreNotInSameResourceGroup_When_AttachNic_Then_ThrowIllegalArgumentException() {
        // Given
        UUID networkId = UUID.randomUUID();
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        ResourceGroupNetwork resourceGroupNetwork = ResourceGroupNetwork.builder()
                .resourceGroup(addId(ResourceGroup.builder().build(), UUID.randomUUID()))
                .build();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(addId(ResourceGroup.builder().build(), UUID.randomUUID()))
                .build();
        when(resourceGroupNetworkRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroupNetwork));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(true);
        // When
        // Then
        assertThrows(ResourceGroupNetworkNotFoundException.class, () -> sut.attachNicToNetwork(networkId, vmId, nicId, ""));
    }

    @Test
    void Given_NicIsNotFromVm_When_AttachNic_Then_DoNothing() {
        // Given
        UUID networkId = UUID.randomUUID();
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        ResourceGroupNetwork resourceGroupNetwork = ResourceGroupNetwork.builder()
                .resourceGroup(addId(ResourceGroup.builder().build(), UUID.randomUUID()))
                .build();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(addId(ResourceGroup.builder().build(), resourceGroupNetwork.getResourceGroup().getId()))
                .build();
        when(resourceGroupNetworkRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroupNetwork));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(true);
        when(oVirtVmService.findNicsByVmId(any()))
                .thenReturn(List.of());
        // When
        sut.attachNicToNetwork(networkId, vmId, nicId, "");
        // Then
        verify(resourceGroupNetworkRepository, never())
                .save(any());
    }

    @Test
    void Given_InputDataIsCorrect_When_AttachNic_Then_AddNetworkInterface() {
        // Given
        UUID networkId = UUID.randomUUID();
        UUID vmId = UUID.randomUUID();
        UUID nicId = UUID.randomUUID();
        Nic nic = Mockito.mock(Nic.class);
        when(nic.id())
                .thenReturn(nicId.toString());
        ResourceGroupNetwork resourceGroupNetwork = ResourceGroupNetwork.builder()
                .resourceGroup(addId(ResourceGroup.builder().build(), UUID.randomUUID()))
                .interfaces(new ArrayList<>())
                .build();
        VirtualMachine virtualMachine = VirtualMachine.builder()
                .resourceGroup(addId(ResourceGroup.builder().build(), resourceGroupNetwork.getResourceGroup().getId()))
                .build();
        when(resourceGroupNetworkRepository.findById(any()))
                .thenReturn(Optional.of(resourceGroupNetwork));
        when(privilegesService.validateResourceGroupOwnership(any()))
                .thenReturn(true);
        when(virtualMachineRepository.findById(any()))
                .thenReturn(Optional.of(virtualMachine));
        when(eTagHelper.validateEtag(any(), any(), anyLong()))
                .thenReturn(true);
        when(oVirtVmService.findNicsByVmId(any()))
                .thenReturn(List.of(nic));
        // When
        sut.attachNicToNetwork(networkId, vmId, nicId, "");
        // Then
        verify(resourceGroupNetworkRepository, times(1))
                .save(any());
        verify(entityManager, times(1))
                .lock(virtualMachine, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    }

    @SneakyThrows
    private <T extends AbstractEntity> T addId(T entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
        return entity;
    }
}
