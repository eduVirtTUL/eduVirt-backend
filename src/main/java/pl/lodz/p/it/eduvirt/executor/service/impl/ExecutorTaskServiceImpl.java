package pl.lodz.p.it.eduvirt.executor.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.entity.subtasks.PermissionTask;
import pl.lodz.p.it.eduvirt.executor.entity.subtasks.VmTask;
import pl.lodz.p.it.eduvirt.executor.entity.subtasks.VnicProfileTask;
import pl.lodz.p.it.eduvirt.executor.repository.ExecutorSubtaskRepository;
import pl.lodz.p.it.eduvirt.executor.repository.ExecutorTaskRepository;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ExecutorTaskServiceImpl implements ExecutorTaskService {

    private final ExecutorTaskRepository executorTaskRepository;
    private final ExecutorSubtaskRepository executorSubtaskRepository;

    @Override
    public ExecutorTask registerPodInitTask(Reservation reservation) {
        return registerNewExecutorTask(reservation, ExecutorTask.TaskType.POD_INIT);
    }

    @Override
    public ExecutorTask registerPodDestroyTask(Reservation reservation) {
        return registerNewExecutorTask(reservation, ExecutorTask.TaskType.POD_DESTRUCT);
    }

    private ExecutorTask registerNewExecutorTask(Reservation reservation, ExecutorTask.TaskType type) {
        return executorTaskRepository.saveAndFlush(new ExecutorTask(reservation, type));
    }

    @Override
    public void finalizeTask(UUID taskId, boolean success, String comment) {
        ExecutorTask task = executorTaskRepository.findById(taskId)
                .orElseThrow(EntityNotFoundException::new);

        if (success) {
            task.setSuccessful();
        } else {
            task.setFailed();
        }
        task.setDescription(
                Objects.nonNull(comment) && !comment.isEmpty() ? comment.substring(0, Math.min(200, comment.length())) : null
        );

        executorTaskRepository.saveAndFlush(task);
    }

    @Override
    public ExecutorSubtask registerSubTask(UUID taskId, UUID vmId, ExecutorSubtask.SubtaskType type) {
        ExecutorTask task = executorTaskRepository.findById(taskId)
                .orElseThrow(RuntimeException::new);

//        ExecutorSubtask subtask = new ExecutorSubtask(task, vmId, type);
        ExecutorSubtask subtask = switch (type) {
            case START_VM, SHUTDOWN_VM, POWER_OFF, REBOOT_VM -> new VmTask(task, vmId, type);
            case ASSIGN_VNIC_PROFILE, REMOVE_VNIC_PROFILE -> new VnicProfileTask(task, vmId, type);
            case ASSIGN_PERMISSION, REVOKE_PERMISSION -> new PermissionTask(task, vmId, type);
        };

        return executorSubtaskRepository.saveAndFlush(subtask);
    }

    @Override
    public void finalizeSubTask(UUID subtaskId, boolean success, String comment, UUID additionalId) {
        ExecutorSubtask subtask = executorSubtaskRepository.findById(subtaskId)
                .orElseThrow(EntityNotFoundException::new);

        subtask.setSuccessful(success);
        subtask.setDescription(
                Objects.nonNull(comment) && !comment.isEmpty() ? comment.substring(0, Math.min(200, comment.length())) : null
        );

        switch (subtask) {
            case VmTask vmTask -> { }
            case VnicProfileTask vnicProfileTask -> {
                vnicProfileTask.setVnicProfileId(additionalId);
            }
            case PermissionTask permissionTask -> { }
            default -> throw new IllegalArgumentException("Unexpected subtask type: " + subtask);
        }

        executorSubtaskRepository.saveAndFlush(subtask);
    }

    @Override
    public List<ExecutorSubtask> getReservationStartExistingSubTasks(Reservation reservation) {
        return executorSubtaskRepository.findByReservation(reservation.getId(), ExecutorTask.TaskType.POD_INIT);
    }

    @Override
    public List<ExecutorSubtask> getReservationEndExistingSubTasks(Reservation reservation) {
        return executorSubtaskRepository.findByReservation(reservation.getId(), ExecutorTask.TaskType.POD_DESTRUCT);
    }
}
