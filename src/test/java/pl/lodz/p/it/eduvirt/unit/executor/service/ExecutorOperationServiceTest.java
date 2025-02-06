package pl.lodz.p.it.eduvirt.unit.executor.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.builders.VmBuilder;
import org.ovirt.engine.sdk4.internal.containers.VmContainer;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VmStatus;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.NetworkInterface;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupNetwork;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.AdditionalId;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.VnicProfileTask;
import pl.lodz.p.it.eduvirt.executor.exception.ResourceGroupCurrentlyInUseException;
import pl.lodz.p.it.eduvirt.executor.exception.VmInvalidStatusException;
import pl.lodz.p.it.eduvirt.executor.exception.VmLaunchingStatusException;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;
import pl.lodz.p.it.eduvirt.executor.service.MailNotificationService;
import pl.lodz.p.it.eduvirt.executor.service.aggregate.impl.ExecutorOperationServiceImpl;
import pl.lodz.p.it.eduvirt.service.ReservationService;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtPermissionService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVmService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExecutorOperationServiceTest {

    @Mock
    private OVirtVmService oVirtVmService;

    @Mock
    private OVirtPermissionService oVirtPermissionService;

    @Mock
    private VnicProfilePoolService vnicProfilePoolService;

    @Mock
    private ReservationService reservationService;

    @Mock
    private ExecutorTaskService executorTaskService;

    @Mock
    private MailNotificationService mailNotificationService;

    @InjectMocks
    private ExecutorOperationServiceImpl executorOperationService;

    /* Test data */

    private Reservation testReservation_1;
    private Reservation testReservation_2;

    private UUID vmId1;
    private UUID vmId2;
    private UUID vmId3;

    private Vm ovirtVm1;
    private Vm ovirtVm2;
    private Vm ovirtVm3;

    private VirtualMachine vm1;
    private VirtualMachine vm2;
    private VirtualMachine vm3;

    private ResourceGroup resourceGroup;

    private ExecutorTask executorTask1;

    private ExecutorSubtask executorSubtask1;
    private ExecutorSubtask executorSubtask2;

    /* Data initialization */

    @BeforeEach
    void setUp() {
        // Reservations
        testReservation_1 = new Reservation();
        setEntityId(testReservation_1, UUID.randomUUID());

        testReservation_2 = new Reservation();
        setEntityId(testReservation_2, UUID.randomUUID());

        // VMs IDs
        vmId1 = UUID.randomUUID();
        vmId2 = UUID.randomUUID();
        vmId3 = UUID.randomUUID();

        // oVirt VMs
        ovirtVm1 = new VmBuilder().id(vmId1.toString()).name("ovirtVm1").build();
        ovirtVm2 = new VmBuilder().id(vmId2.toString()).name("ovirtVm2").build();
        ovirtVm3 = new VmBuilder().id(vmId3.toString()).name("ovirtVm3").build();

        // eduVirt VMs
        vm1 = new VirtualMachine();
        vm1.setId(vmId1);

        vm2 = new VirtualMachine();
        vm2.setId(vmId2);

        vm3 = new VirtualMachine();
        vm3.setId(vmId3);

        // RGs
        resourceGroup = new ResourceGroup();

        // Executor tasks
        executorTask1 = new ExecutorTask();
        setEntityId(executorTask1, UUID.randomUUID());

        // Executor subtasks
        executorSubtask1 = new VnicProfileTask();
        setEntityId(executorSubtask1, UUID.randomUUID());

        executorSubtask2 = new VnicProfileTask();
        setEntityId(executorSubtask2, UUID.randomUUID());
    }

    /* Private methods */

    @Test
    void Given_ResourceGroupNotAssociatedWithOtherReservations_When_CheckIfRgIsInUse_Then_Success() {
        when(reservationService.findRgNotCompletedReservations(resourceGroup))
                .thenReturn(List.of(testReservation_1));

        executorOperationServiceMethod("checkIfRgIsInUse", testReservation_1.getId(), resourceGroup);

        verify(reservationService, times(1)).findRgNotCompletedReservations(resourceGroup);
    }

    @Test
    void Given_ResourceGroupNotAssociatedWithAnyReservations_When_CheckIfRgIsInUse_Then_Success() {
        when(reservationService.findRgNotCompletedReservations(resourceGroup))
                .thenReturn(List.of());

        executorOperationServiceMethod("checkIfRgIsInUse", testReservation_1.getId(), resourceGroup);

        verify(reservationService, times(1)).findRgNotCompletedReservations(resourceGroup);
    }

    @Test
    void Given_ResourceGroupAssociatedWithOtherReservations_When_CheckIfRgIsInUse_Then_ThrowException() {
        when(reservationService.findRgNotCompletedReservations(resourceGroup))
                .thenReturn(List.of(testReservation_2));

        assertThrows(ResourceGroupCurrentlyInUseException.class,
                () -> executorOperationServiceMethod("checkIfRgIsInUse", testReservation_1.getId(), resourceGroup)
        );

        verify(reservationService, times(1)).findRgNotCompletedReservations(resourceGroup);
    }

    @Test
    void Given_ValidData_When_MapNetworkToVnicProfileWithoutFiltering_Then_Success() {
        /// ///
        ResourceGroupNetwork network = ResourceGroupNetwork.builder()
                .name("net1")
                .build();

        NetworkInterface nic1Vm1 = NetworkInterface.builder()
                .id(UUID.randomUUID())
                .virtualMachine(vm1)
                .resourceGroupNetwork(network)
                .build();

        NetworkInterface nic1Vm2 = NetworkInterface.builder()
                .id(UUID.randomUUID())
                .virtualMachine(vm2)
                .resourceGroupNetwork(network)
                .build();

        network.setInterfaces(Arrays.asList(nic1Vm1, nic1Vm2));

        VnicProfilePoolMember freeVnicProfileFromPool = new VnicProfilePoolMember(
                UUID.randomUUID(),
                101,
                "vnicProfile1",
                "network1"
        );
        /// ///

        when(vnicProfilePoolService.getFirstFreeVnicProfileFromPool())
                .thenReturn(Optional.of(freeVnicProfileFromPool));

        doNothing().when(vnicProfilePoolService).markVnicProfileAsOccupied(freeVnicProfileFromPool.getId());

        /* Run and register section */
        when(executorTaskService.registerSubTask(executorTask1.getId(), vmId1, ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE))
                .thenReturn(executorSubtask1);
        when(executorTaskService.registerSubTask(executorTask1.getId(), vmId2, ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE))
                .thenReturn(executorSubtask2);

        doNothing().when(executorTaskService).finalizeSubTask(executorSubtask1.getId(), true,
                AdditionalId.VNIC_PROFILE.withId(freeVnicProfileFromPool.getId()),
                AdditionalId.NIC.withId(nic1Vm1.getId())
        );

        doNothing().when(executorTaskService).finalizeSubTask(executorSubtask2.getId(), true,
                AdditionalId.VNIC_PROFILE.withId(freeVnicProfileFromPool.getId()),
                AdditionalId.NIC.withId(nic1Vm2.getId())
        );

        /* Run and register section */

        executorOperationServiceMethod("mapNetworkToVnicProfile", network, new HashMap<>(), executorTask1);

        // Verifications
        verify(vnicProfilePoolService, times(1)).getFirstFreeVnicProfileFromPool();
        verify(vnicProfilePoolService, times(1)).markVnicProfileAsOccupied(freeVnicProfileFromPool.getId());

        verify(executorTaskService, times(2))
                .registerSubTask(eq(executorTask1.getId()), any(UUID.class), eq(ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE));
        verify(executorTaskService, times(1))
                .registerSubTask(executorTask1.getId(), vmId1, ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE);
        verify(executorTaskService, times(1))
                .registerSubTask(executorTask1.getId(), vmId2, ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE);

        verify(executorTaskService, times(2)).finalizeSubTask(
                any(UUID.class), eq(true),
                eq(AdditionalId.VNIC_PROFILE.withId(freeVnicProfileFromPool.getId())),
                any(AdditionalId.NIC.getDeclaringClass())
        );

        verify(executorTaskService, times(1)).finalizeSubTask(
                executorSubtask1.getId(), true,
                AdditionalId.VNIC_PROFILE.withId(freeVnicProfileFromPool.getId()),
                AdditionalId.NIC.withId(nic1Vm1.getId())
        );

        verify(executorTaskService, times(1)).finalizeSubTask(
                executorSubtask2.getId(), true,
                AdditionalId.VNIC_PROFILE.withId(freeVnicProfileFromPool.getId()),
                AdditionalId.NIC.withId(nic1Vm2.getId())
        );
    }

    /* Methods related to oVirt Api calls */

    @Test
    void Given_VirtualMachines_When_FetchOvirtVms_Then_ReturnOvirtVms() {
        Set<String> vmIds = Set.of(vmId1.toString(), vmId2.toString());

        when(oVirtVmService.findVmsWithNicsByVmIds(vmIds))
                .thenReturn(List.of(ovirtVm1, ovirtVm2));

        @SuppressWarnings("unchecked")
        List<Vm> result = executorOperationServiceMethod("fetchOvirtVms", List.class, new ArrayList<>(List.of(vm1, vm2)));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(ovirtVm1));
        assertTrue(result.contains(ovirtVm2));

        verify(oVirtVmService, times(1)).findVmsWithNicsByVmIds(vmIds);
    }

    @Test
    void Given_OvirtVmsWithValidStatuses_CheckIfVmsDownStatus_Then_Success() {
        ((VmContainer) ovirtVm1).status(VmStatus.DOWN);
        ((VmContainer) ovirtVm2).status(VmStatus.DOWN);
        ((VmContainer) ovirtVm3).status(VmStatus.DOWN);

        executorOperationServiceMethod("checkIfVmsDownStatus", new ArrayList<>(List.of(ovirtVm1, ovirtVm2, ovirtVm3)));
    }

    @Test
    void Given_EmptyList_CheckIfVmsDownStatus_Then_Success() {
        executorOperationServiceMethod("checkIfVmsDownStatus", new ArrayList<>());
    }

    @Test
    void Given_OvirtVmsWithInvalidStatuses_CheckIfVmsDownStatus_Then_ThrowException() {
        ((VmContainer) ovirtVm1).status(VmStatus.UP);
        ((VmContainer) ovirtVm2).status(VmStatus.REBOOT_IN_PROGRESS);
        ((VmContainer) ovirtVm3).status(VmStatus.POWERING_UP);

        VmInvalidStatusException ex = assertThrowsExactly(VmInvalidStatusException.class,
                () -> executorOperationServiceMethod("checkIfVmsDownStatus", new ArrayList<>(List.of(ovirtVm1, ovirtVm2, ovirtVm3)))
        );

        assertNotNull(ex);
        assertEquals("Some VMs are in invalid statuses: VM %s in %s status"
                        .formatted(ovirtVm1.name(), VmStatus.UP.name()),
                ex.getMessage()
        );
    }

    @Test
    void Given_OvirtVmsWithOnlyLaunchingStatuses_CheckIfVmsDownStatus_Then_ThrowException() {
        ((VmContainer) ovirtVm1).status(VmStatus.DOWN);
        ((VmContainer) ovirtVm2).status(VmStatus.REBOOT_IN_PROGRESS);
        ((VmContainer) ovirtVm3).status(VmStatus.POWERING_UP);

        VmLaunchingStatusException ex = assertThrows(VmLaunchingStatusException.class,
                () -> executorOperationServiceMethod("checkIfVmsDownStatus", new ArrayList<>(List.of(ovirtVm1, ovirtVm2, ovirtVm3)))
        );

        assertNotNull(ex);
        assertTrue(
                "Some VMs are in launching statuses: VM %s in %s status;VM %s in %s status"
                        .formatted(ovirtVm2.name(), VmStatus.REBOOT_IN_PROGRESS.name(),
                                ovirtVm3.name(), VmStatus.POWERING_UP.name()).equals(ex.getMessage())
                        ||
                        "Some VMs are in launching statuses: VM %s in %s status;VM %s in %s status"
                                .formatted(ovirtVm3.name(), VmStatus.POWERING_UP.name(),
                                        ovirtVm2.name(), VmStatus.REBOOT_IN_PROGRESS.name()).equals(ex.getMessage())
        );
    }

    @Test
    void Given_ValidData_AssignVnicProfileToNIC_Then_Success() {
        UUID vnicProfileId = UUID.randomUUID();
        UUID vmId = UUID.randomUUID();
        UUID vmNicId = UUID.randomUUID();

        doNothing().when(oVirtVmService)
                .assignVnicProfileToVm(vmId.toString(), vmNicId.toString(), vnicProfileId.toString());

        executorOperationServiceMethod("assignVnicProfileToNIC", vnicProfileId, vmId, vmNicId);

        verify(oVirtVmService, times(1)).assignVnicProfileToVm(vmId.toString(), vmNicId.toString(), vnicProfileId.toString());
    }

    @Test
    void Given_ValidData_AddTeamPermissionsToVm_Then_Success() {
        UUID vmId = UUID.randomUUID();
        UUID userId_1 = UUID.randomUUID();
        UUID userId_2 = UUID.randomUUID();

        doNothing().when(oVirtPermissionService)
                .assignPermissionToVmToUser(eq(vmId), any(UUID.class), anyString());

        executorOperationServiceMethod("addTeamPermissionsToVm", vmId, new ArrayList<>(List.of(userId_1, userId_2)));

        verify(oVirtPermissionService, times(2)).assignPermissionToVmToUser(eq(vmId), any(UUID.class), anyString());
        verify(oVirtPermissionService, times(1)).assignPermissionToVmToUser(eq(vmId), eq(userId_1), anyString());
        verify(oVirtPermissionService, times(1)).assignPermissionToVmToUser(eq(vmId), eq(userId_2), anyString());
    }

    /* Utils */

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private <T> void setField(Class<T> clazz, Object object, String fieldName, Object value) {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
        field.setAccessible(false);
    }

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        setField(AbstractEntity.class, entity, "id", id);
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private <T, R> R executeMethod(Class<T> clazz, Object object,
                                   String methodName, Class<R> returnType, Object... parameters) {
        Class<?>[] parameterTypes = Arrays.stream(parameters)
                .map(ExecutorOperationServiceTest::mapClassTypes)
                .toList().toArray(new Class[0]);
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object result;
        try {
            result = method.invoke(object, parameters);
        } catch (Throwable e) {
            throw e.getCause();
        }
        method.setAccessible(false);
        return returnType.cast(result);
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private <T> void executeMethod(Class<T> clazz, Object object,
                                   String methodName, Object... parameters) {
        executeMethod(clazz, object, methodName, Void.class, parameters);
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private <R> R executorOperationServiceMethod(String methodName, Class<R> returnType, Object... parameters) {
        return executeMethod(executorOperationService.getClass(), executorOperationService,
                methodName, returnType, parameters);
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private void executorOperationServiceMethod(String methodName, Object... parameters) {
        executeMethod(executorOperationService.getClass(), executorOperationService, methodName, parameters);
    }

    private static Class<?> mapClassTypes(Object obj) {
        return switch (obj) {
            case ArrayList<?> ignored -> List.class;
            case HashMap<?, ?> ignored -> Map.class;
            case HashSet<?> ignored -> Set.class;
            default -> obj.getClass();
        };
    }
}
