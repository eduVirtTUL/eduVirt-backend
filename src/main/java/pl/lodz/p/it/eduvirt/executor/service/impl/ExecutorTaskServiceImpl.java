package pl.lodz.p.it.eduvirt.executor.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.repository.ExecutorSubtaskRepository;
import pl.lodz.p.it.eduvirt.executor.repository.ExecutorTaskRepository;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;

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
    public void registerSubTask(UUID taskId, UUID vmId, ExecutorSubtask.SubtaskType type, boolean success, String comment) {
        ExecutorTask task = executorTaskRepository.findById(taskId)
                .orElseThrow(EntityNotFoundException::new);

        ExecutorSubtask subtask = new ExecutorSubtask(task, vmId, type);
        subtask.setSuccessful(success);
        subtask.setDescription(
                Objects.nonNull(comment) && !comment.isEmpty() ? comment.substring(0, Math.min(200, comment.length())) : null
        );

        executorSubtaskRepository.saveAndFlush(subtask);
    }
}
