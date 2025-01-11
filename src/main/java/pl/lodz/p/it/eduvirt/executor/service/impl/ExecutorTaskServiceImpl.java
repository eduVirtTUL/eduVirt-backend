package pl.lodz.p.it.eduvirt.executor.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.entity.subtasks.AdditionalId;
import pl.lodz.p.it.eduvirt.executor.entity.subtasks.PermissionTask;
import pl.lodz.p.it.eduvirt.executor.entity.subtasks.VmTask;
import pl.lodz.p.it.eduvirt.executor.entity.subtasks.VnicProfileTask;
import pl.lodz.p.it.eduvirt.executor.repository.ExecutorSubtaskRepository;
import pl.lodz.p.it.eduvirt.executor.repository.ExecutorTaskRepository;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

        // TODO michal: Ask is better save nulls or mapping nulls to 00000000-0000-0000-0000-000000000000
        UUID sanitizedVmId = Objects.requireNonNullElse(vmId, UUID.fromString("00000000-0000-0000-0000-000000000000"));

        ExecutorSubtask subtask = switch (type) {
            case START_VM, SHUTDOWN_VM, POWER_OFF, REBOOT_VM -> new VmTask(task, sanitizedVmId, type);
            case ASSIGN_VNIC_PROFILE, REMOVE_VNIC_PROFILE -> new VnicProfileTask(task, sanitizedVmId, type);
            case ASSIGN_PERMISSION, REVOKE_PERMISSION -> new PermissionTask(task, sanitizedVmId, type);
        };

        return executorSubtaskRepository.saveAndFlush(subtask);
    }

    @Override
    public void finalizeSubTask(UUID subtaskId, boolean success, String comment, AdditionalId... additionalIds) {
        ExecutorSubtask subtask = executorSubtaskRepository.findById(subtaskId)
                .orElseThrow(EntityNotFoundException::new);

        subtask.setSuccessful(success);
        subtask.setDescription(
                Objects.nonNull(comment) && !comment.isEmpty() ? comment.substring(0, Math.min(200, comment.length())) : null
        );

        Map<AdditionalId, UUID> mapOfAdditionalIds = new HashMap<>();
        if (Objects.nonNull(additionalIds)) {
            for (AdditionalId additionalId : additionalIds) {
                mapOfAdditionalIds.put(
                        additionalId,
                        // TODO michal: Ask is better save nulls or mapping nulls to 00000000-0000-0000-0000-000000000000
                        Optional.ofNullable(additionalId.getId())
                                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                );
            }
        }

        switch (subtask) {
            case VmTask vmTask -> {
            }
            case VnicProfileTask vnicProfileTask -> {
                vnicProfileTask.setVnicProfileId(mapOfAdditionalIds.get(AdditionalId.VNIC_PROFILE));
                vnicProfileTask.setNicId(mapOfAdditionalIds.get(AdditionalId.NIC));
            }
            case PermissionTask permissionTask -> {
            }
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

    @Override
    public List<ExecutorTask> getReservationsToEndTasks() {
        return executorTaskRepository.findReservationsToEndTasks();
    }
}
