package pl.lodz.p.it.eduvirt.unit.executor.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupNetwork;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.executor.entity.mails.MailNotification;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.AdditionalId;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.PermissionTask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.PreconditionsCheckTask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.VmTask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.VnicProfileTask;
import pl.lodz.p.it.eduvirt.executor.repository.ExecutorSubtaskRepository;
import pl.lodz.p.it.eduvirt.executor.repository.ExecutorTaskRepository;
import pl.lodz.p.it.eduvirt.executor.repository.MailNotificationRepository;
import pl.lodz.p.it.eduvirt.executor.service.impl.ExecutorTaskServiceImpl;
import pl.lodz.p.it.eduvirt.executor.service.impl.MailNotificationServiceImpl;
import pl.lodz.p.it.eduvirt.util.MailProvider;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExecutorTaskServiceTest {

    @Mock
    private ExecutorTaskRepository executorTaskRepository;

    @Mock
    private ExecutorSubtaskRepository executorSubtaskRepository;

    @InjectMocks
    private ExecutorTaskServiceImpl executorTaskService;

    /* Test data */

    private Reservation testReservation;

    /* Data initialization */

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(executorTaskService, "vmShutdownGraceTime", 2);

        testReservation = new Reservation();
        setEntityId(testReservation, UUID.randomUUID());
    }

    /* Tests */

    @Test
    void Given_Reservation_When_RegisterPodInitTask_Then_Success() {
        ExecutorTask executorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        when(executorTaskRepository.saveAndFlush(any(ExecutorTask.class)))
                .thenReturn(executorTask);

        ExecutorTask result = executorTaskService.registerPodInitTask(testReservation);

        assertNotNull(result);
        assertEquals(result, executorTask);

        verify(executorTaskRepository, times(1)).saveAndFlush(any(ExecutorTask.class));
    }

    @Test
    void Given_Reservation_When_RegisterPodDestroyTask_Then_Success() {
        ExecutorTask executorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_DESTRUCT);
        when(executorTaskRepository.saveAndFlush(any(ExecutorTask.class)))
                .thenReturn(executorTask);

        ExecutorTask result = executorTaskService.registerPodDestroyTask(testReservation);

        assertNotNull(result);
        assertEquals(result, executorTask);

        verify(executorTaskRepository, times(1)).saveAndFlush(any(ExecutorTask.class));
    }

    @Test
    void Given_Reservation_When_RegisterEndReservationTask_Then_Success() {
        ExecutorTask executorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.END_RESERVATION);
        when(executorTaskRepository.saveAndFlush(any(ExecutorTask.class)))
                .thenReturn(executorTask);

        ExecutorTask result = executorTaskService.registerEndReservationTask(testReservation);

        assertNotNull(result);
        assertEquals(result, executorTask);

        verify(executorTaskRepository, times(1)).saveAndFlush(any(ExecutorTask.class));
    }

    @Test
    void Given_ExistingTaskIdAndNullComment_When_FinalizeTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        when(executorTaskRepository.saveAndFlush(any(ExecutorTask.class)))
                .thenReturn(testExecutorTask);

        assertEquals(ExecutorTask.TaskStatus.IN_PROGRESS, testExecutorTask.getStatus());
        assertNull(testExecutorTask.getDescription());

        executorTaskService.finalizeTask(taskId, true, null);

        assertEquals(ExecutorTask.TaskStatus.SUCCESSFUL, testExecutorTask.getStatus());
        assertNull(testExecutorTask.getDescription());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorTaskRepository, times(1)).saveAndFlush(any(ExecutorTask.class));
    }

    @Test
    void Given_ExistingTaskIdAndBlankComment_When_FinalizeTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        when(executorTaskRepository.saveAndFlush(any(ExecutorTask.class)))
                .thenReturn(testExecutorTask);

        assertEquals(ExecutorTask.TaskStatus.IN_PROGRESS, testExecutorTask.getStatus());
        assertNull(testExecutorTask.getDescription());

        executorTaskService.finalizeTask(taskId, true, " ");

        assertEquals(ExecutorTask.TaskStatus.SUCCESSFUL, testExecutorTask.getStatus());
        assertNull(testExecutorTask.getDescription());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorTaskRepository, times(1)).saveAndFlush(any(ExecutorTask.class));
    }

    @Test
    void Given_ExistingTaskIdAndComment_When_FinalizeTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        when(executorTaskRepository.saveAndFlush(any(ExecutorTask.class)))
                .thenReturn(testExecutorTask);

        assertEquals(ExecutorTask.TaskStatus.IN_PROGRESS, testExecutorTask.getStatus());
        assertNull(testExecutorTask.getDescription());

        executorTaskService.finalizeTask(taskId, false, "Test comment");

        assertEquals(ExecutorTask.TaskStatus.FAILED, testExecutorTask.getStatus());
        assertEquals("Test comment", testExecutorTask.getDescription());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorTaskRepository, times(1)).saveAndFlush(any(ExecutorTask.class));
    }

    @Test
    void Given_ExistingTaskIdAndTooLongComment_When_FinalizeTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        when(executorTaskRepository.saveAndFlush(any(ExecutorTask.class)))
                .thenReturn(testExecutorTask);

        assertEquals(ExecutorTask.TaskStatus.IN_PROGRESS, testExecutorTask.getStatus());
        assertNull(testExecutorTask.getDescription());

        executorTaskService.finalizeTask(taskId, false, "A".repeat(250));

        assertEquals(ExecutorTask.TaskStatus.FAILED, testExecutorTask.getStatus());
        assertEquals("A".repeat(200), testExecutorTask.getDescription());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorTaskRepository, times(1)).saveAndFlush(any(ExecutorTask.class));
    }

    @Test
    void Given_NonExistingTaskId_When_FinalizeTask_Then_ThrowException() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> executorTaskService.finalizeTask(taskId, true, null)
        );

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorTaskRepository, times(0)).saveAndFlush(any(ExecutorTask.class));
    }

    @Test
    void Given_ValidData_When_RegisterSubTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        UUID vmId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        ExecutorSubtask testExecutorSubTask = new VmTask(testExecutorTask, vmId,
                ExecutorSubtask.SubtaskType.START_VM
        );

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubTask);

        ExecutorSubtask result = executorTaskService.registerSubTask(taskId, vmId,
                ExecutorSubtask.SubtaskType.START_VM
        );

        assertNotNull(result);
        assertEquals(testExecutorSubTask, result);

        assertEquals(vmId, result.getVmId());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidData_2_When_RegisterSubTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        UUID vmId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        ExecutorSubtask testExecutorSubTask = new VnicProfileTask(testExecutorTask, vmId,
                ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE
        );

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubTask);

        ExecutorSubtask result = executorTaskService.registerSubTask(taskId, vmId,
                ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE
        );

        assertNotNull(result);
        assertEquals(testExecutorSubTask, result);

        assertEquals(vmId, result.getVmId());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidData_3_When_RegisterSubTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        UUID vmId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        ExecutorSubtask testExecutorSubTask = new PermissionTask(testExecutorTask, vmId,
                ExecutorSubtask.SubtaskType.ASSIGN_PERMISSION
        );

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubTask);

        ExecutorSubtask result = executorTaskService.registerSubTask(taskId, vmId,
                ExecutorSubtask.SubtaskType.ASSIGN_PERMISSION
        );

        assertNotNull(result);
        assertEquals(testExecutorSubTask, result);

        assertEquals(vmId, result.getVmId());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidData_4_When_RegisterSubTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        ExecutorSubtask testExecutorSubTask = new PreconditionsCheckTask(testExecutorTask,
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE
        );

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubTask);

        ExecutorSubtask result = executorTaskService.registerSubTask(taskId, null,
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE
        );

        assertNotNull(result);
        assertEquals(testExecutorSubTask, result);

        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), result.getVmId());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_NulledVmId_When_RegisterSubTask_Then_Success() {
        ExecutorTask testExecutorTask = new ExecutorTask(testReservation, ExecutorTask.TaskType.POD_INIT);
        UUID taskId = UUID.randomUUID();
        setEntityId(testExecutorTask, taskId);

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.of(testExecutorTask));

        UUID sanitizedVmId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        ExecutorSubtask testExecutorSubTask = new VmTask(testExecutorTask, sanitizedVmId,
                ExecutorSubtask.SubtaskType.START_VM
        );

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubTask);

        ExecutorSubtask result = executorTaskService.registerSubTask(taskId, null,
                ExecutorSubtask.SubtaskType.START_VM
        );

        assertNotNull(result);
        assertEquals(testExecutorSubTask, result);

        assertEquals(sanitizedVmId, result.getVmId());

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_NonExistingTaskId_When_RegisterSubTask_Then_ThrowException() {
        UUID taskId = UUID.randomUUID();

        when(executorTaskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        UUID vmId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertThrows(EntityNotFoundException.class,
                () -> executorTaskService.registerSubTask(taskId, vmId, ExecutorSubtask.SubtaskType.START_VM)
        );

        verify(executorTaskRepository, times(1)).findById(taskId);
        verify(executorSubtaskRepository, times(0)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidData_When_FinalizeSubTask_Then_Success() {
        UUID vmId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID nicId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID vnicProfileId = UUID.fromString("00000000-0000-0000-0000-000000000003");

        VnicProfileTask testExecutorSubtask = new VnicProfileTask(new ExecutorTask(), vmId,
                ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getNicId());
        assertNull(testExecutorSubtask.getVnicProfileId());

        executorTaskService.finalizeSubTask(subTaskId, true,
                AdditionalId.NIC.withId(nicId), AdditionalId.VNIC_PROFILE.withId(vnicProfileId)
        );

        assertTrue(testExecutorSubtask.getSuccessful());
        assertEquals(nicId, testExecutorSubtask.getNicId());
        assertEquals(vnicProfileId, testExecutorSubtask.getVnicProfileId());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidData_1_When_FinalizeSubTask_Then_Success() {
        UUID vmId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        VmTask testExecutorSubtask = new VmTask(new ExecutorTask(), vmId,
                ExecutorSubtask.SubtaskType.START_VM);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());

        executorTaskService.finalizeSubTask(subTaskId, true);

        assertTrue(testExecutorSubtask.getSuccessful());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidData_2_When_FinalizeSubTask_Then_Success() {
        UUID vmId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        PermissionTask testExecutorSubtask = new PermissionTask(new ExecutorTask(), vmId,
                ExecutorSubtask.SubtaskType.ASSIGN_PERMISSION);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());

        executorTaskService.finalizeSubTask(subTaskId, true);

        assertTrue(testExecutorSubtask.getSuccessful());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidData_3_When_FinalizeSubTask_Then_Success() {
        PreconditionsCheckTask testExecutorSubtask = new PreconditionsCheckTask(new ExecutorTask(),
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());

        executorTaskService.finalizeSubTask(subTaskId, true);

        assertTrue(testExecutorSubtask.getSuccessful());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidDataWithValidComment_When_FinalizeSubTask_Then_Success() {
        PreconditionsCheckTask testExecutorSubtask = new PreconditionsCheckTask(new ExecutorTask(),
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getDescription());

        executorTaskService.finalizeSubTask(subTaskId, false, "Test comment");

        assertFalse(testExecutorSubtask.getSuccessful());
        assertEquals("Test comment", testExecutorSubtask.getDescription());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidDataWithTooLongComment_When_FinalizeSubTask_Then_Success() {
        PreconditionsCheckTask testExecutorSubtask = new PreconditionsCheckTask(new ExecutorTask(),
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getDescription());

        executorTaskService.finalizeSubTask(subTaskId, false, "A".repeat(550));

        assertFalse(testExecutorSubtask.getSuccessful());
        assertEquals("A".repeat(500), testExecutorSubtask.getDescription());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidDataWithNullComment_When_FinalizeSubTask_Then_Success() {
        PreconditionsCheckTask testExecutorSubtask = new PreconditionsCheckTask(new ExecutorTask(),
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getDescription());

        executorTaskService.finalizeSubTask(subTaskId, false, null, new AdditionalId[0]);

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getDescription());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidDataWithBlankComment_When_FinalizeSubTask_Then_Success() {
        PreconditionsCheckTask testExecutorSubtask = new PreconditionsCheckTask(new ExecutorTask(),
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getDescription());

        executorTaskService.finalizeSubTask(subTaskId, false, " ");

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getDescription());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ValidDataWithNullVarArgs_When_FinalizeSubTask_Then_Success() {
        PreconditionsCheckTask testExecutorSubtask = new PreconditionsCheckTask(new ExecutorTask(),
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        when(executorSubtaskRepository.saveAndFlush(any(ExecutorSubtask.class)))
                .thenReturn(testExecutorSubtask);

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getDescription());

        executorTaskService.finalizeSubTask(subTaskId, false, (AdditionalId[]) null);

        assertFalse(testExecutorSubtask.getSuccessful());
        assertNull(testExecutorSubtask.getDescription());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(1)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_NonExistingSubTaskId_When_FinalizeSubTask_Then_ThrowException() {
        PreconditionsCheckTask testExecutorSubtask = new PreconditionsCheckTask(new ExecutorTask(),
                ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE);
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.empty());

        assertFalse(testExecutorSubtask.getSuccessful());

        assertThrows(EntityNotFoundException.class,
                () -> executorTaskService.finalizeSubTask(subTaskId, true)
        );

        assertFalse(testExecutorSubtask.getSuccessful());

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(0)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_ExistingSubTaskButInvalidSubtaskType_When_FinalizeSubTask_Then_ThrowException() {
        class FakeSubtask extends ExecutorSubtask {}

        ExecutorSubtask testExecutorSubtask = new FakeSubtask();
        UUID subTaskId = UUID.randomUUID();
        setEntityId(testExecutorSubtask, subTaskId);

        when(executorSubtaskRepository.findById(subTaskId))
                .thenReturn(Optional.of(testExecutorSubtask));

        assertThrows(IllegalArgumentException.class,
                () -> executorTaskService.finalizeSubTask(subTaskId, true)
        );

        verify(executorSubtaskRepository, times(1)).findById(subTaskId);
        verify(executorSubtaskRepository, times(0)).saveAndFlush(any(ExecutorSubtask.class));
    }

    @Test
    void Given_SubTasksExist_When_GetReservationStartExistingSubTasks_Then_ReturnSubTasks() {
        when(executorSubtaskRepository.findByReservation(testReservation.getId(), ExecutorTask.TaskType.POD_INIT))
                .thenReturn(List.of(new VmTask(), new VnicProfileTask(), new PermissionTask()));

        List<ExecutorSubtask> resultList = executorTaskService.getReservationStartExistingSubTasks(testReservation);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
    }

    @Test
    void Given_NoSubTasksExist_When_GetReservationStartExistingSubTasks_Then_ReturnEmptyList() {
        when(executorSubtaskRepository.findByReservation(testReservation.getId(), ExecutorTask.TaskType.POD_INIT))
                .thenReturn(List.of());

        List<ExecutorSubtask> resultList = executorTaskService.getReservationStartExistingSubTasks(testReservation);

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void Given_SubTasksExist_When_GetStopPodExistingSubTasks_Then_ReturnSubTasks() {
        when(executorSubtaskRepository.findByReservation(testReservation.getId(), ExecutorTask.TaskType.POD_DESTRUCT))
                .thenReturn(List.of(new VmTask(), new VnicProfileTask(), new PermissionTask()));

        List<ExecutorSubtask> resultList = executorTaskService.getStopPodExistingSubTasks(testReservation);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
    }

    @Test
    void Given_NoSubTasksExist_When_GetStopPodExistingSubTasks_Then_ReturnEmptyList() {
        when(executorSubtaskRepository.findByReservation(testReservation.getId(), ExecutorTask.TaskType.POD_DESTRUCT))
                .thenReturn(List.of());

        List<ExecutorSubtask> resultList = executorTaskService.getStopPodExistingSubTasks(testReservation);

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void Given_SubTasksExist_When_GetReservationEndExistingSubTasks_Then_ReturnSubTasks() {
        when(executorSubtaskRepository.findByReservation(testReservation.getId(), ExecutorTask.TaskType.END_RESERVATION))
                .thenReturn(List.of(new VmTask(), new VnicProfileTask(), new PermissionTask()));

        List<ExecutorSubtask> resultList = executorTaskService.getReservationEndExistingSubTasks(testReservation);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
    }

    @Test
    void Given_NoSubTasksExist_When_GetReservationEndExistingSubTasks_Then_ReturnEmptyList() {
        when(executorSubtaskRepository.findByReservation(testReservation.getId(), ExecutorTask.TaskType.END_RESERVATION))
                .thenReturn(List.of());

        List<ExecutorSubtask> resultList = executorTaskService.getReservationEndExistingSubTasks(testReservation);

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void Given_SubTasksExist_When_GetReservationsToEndTasks_Then_ReturnSubTasks() {
        Reservation testReservationFullData1 = getFullDataReservation();
        Reservation testReservationFullData2 = getFullDataReservation();

        ExecutorTask testExecutorTask1 = new ExecutorTask(testReservationFullData1, ExecutorTask.TaskType.END_RESERVATION);
        UUID taskId1 = UUID.randomUUID();
        setEntityId(testExecutorTask1, taskId1);

        ExecutorTask testExecutorTask2 = new ExecutorTask(testReservationFullData2, ExecutorTask.TaskType.END_RESERVATION);
        UUID taskId2 = UUID.randomUUID();
        setEntityId(testExecutorTask1, taskId2);

        when(executorTaskRepository.findReservationsToEndTasks(any(LocalDateTime.class)))
                .thenReturn(List.of(testExecutorTask1, testExecutorTask2));

        List<ExecutorTask> resultList = executorTaskService.getReservationsToEndTasks();

        assertNotNull(resultList);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(testExecutorTask1));
        assertTrue(resultList.contains(testExecutorTask2));
    }

    @Test
    void Given_NoSubTasksExist_When_GetReservationsToEndTasks_Then_ReturnEmptyList() {
        when(executorTaskRepository.findReservationsToEndTasks(any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<ExecutorTask> resultList = executorTaskService.getReservationsToEndTasks();

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void Given_TasksExist_When_GetReservationsInProgressTasks_Then_ReturnTasks() {
        when(executorTaskRepository.findReservationsInProgressTasks())
                .thenReturn(List.of(new ExecutorTask(), new ExecutorTask()));

        List<ExecutorTask> resultList = executorTaskService.getReservationsInProgressTasks();

        assertNotNull(resultList);
        assertEquals(2, resultList.size());
    }

    @Test
    void Given_NoTasksExist_When_GetReservationsInProgressTasks_Then_ReturnEmptyList() {
        when(executorTaskRepository.findReservationsInProgressTasks())
                .thenReturn(List.of());

        List<ExecutorTask> resultList = executorTaskService.getReservationsInProgressTasks();

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    void Given_SubTasksExist_When_GetReservationsInProgressSubTasks_Then_ReturnSubTasks() {
        when(executorSubtaskRepository.findReservationsInProgressSubTasks())
                .thenReturn(List.of(new VmTask(), new VnicProfileTask(), new PermissionTask()));

        List<ExecutorSubtask> resultList = executorTaskService.getReservationsInProgressSubTasks();

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
    }

    @Test
    void Given_NoSubTasksExist_When_GetReservationsInProgressSubTasks_Then_ReturnEmptyList() {
        when(executorSubtaskRepository.findReservationsInProgressSubTasks())
                .thenReturn(List.of());

        List<ExecutorSubtask> resultList = executorTaskService.getReservationsInProgressSubTasks();

        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    /* Utils */

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        setField(AbstractEntity.class, entity, "id", id);
    }

    @SneakyThrows
    private <T> void setField(Class<T> clazz, Object object, String fieldName, Object value) {
        Field idField = clazz.getDeclaredField(fieldName);
        idField.setAccessible(true);
        idField.set(object, value);
        idField.setAccessible(false);
    }

    private Reservation getFullDataReservation() {
        Reservation newReservation = new Reservation();
        setEntityId(newReservation, UUID.randomUUID());

        ResourceGroup resourceGroup = new ResourceGroup();
        resourceGroup.setVms(new ArrayList<>());
        resourceGroup.setNetworks(new ArrayList<>());

        Team team = new Team();
        team.setUsers(new ArrayList<>());

        newReservation.setResourceGroup(resourceGroup);
        newReservation.setTeam(team);

        return newReservation;
    }
}
