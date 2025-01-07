package pl.lodz.p.it.eduvirt.executor.service;

import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.entity.subtasks.AdditionalId;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ExecutorTaskService {

    ExecutorTask registerPodInitTask(Reservation reservation);

    ExecutorTask registerPodDestroyTask(Reservation reservation);

    void finalizeTask(UUID taskId, boolean success, String comment);

    ExecutorSubtask registerSubTask(UUID taskId, UUID vmId, ExecutorSubtask.SubtaskType type);

    void finalizeSubTask(UUID subtaskId, boolean success, String comment, AdditionalId... additionalIds);

    List<ExecutorSubtask> getReservationStartExistingSubTasks(Reservation reservation);

    List<ExecutorSubtask> getReservationEndExistingSubTasks(Reservation reservation);

    List<ExecutorTask> getReservationsToEndTasks();
}
