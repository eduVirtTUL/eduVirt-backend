package pl.lodz.p.it.eduvirt.executor.service;

import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;

import java.util.List;
import java.util.UUID;

public interface ExecutorTaskService {

    ExecutorTask registerPodInitTask(Reservation reservation);

    ExecutorTask registerPodDestroyTask(Reservation reservation);

    void finalizeTask(UUID taskId, boolean success, String comment);

    ExecutorSubtask registerSubTask(UUID taskId, UUID vmId, ExecutorSubtask.SubtaskType type);

    void finalizeSubTask(UUID subtaskId, boolean success, String comment, UUID additionalId);

    List<ExecutorSubtask> getReservationStartExistingSubTasks(Reservation reservation);

    List<ExecutorSubtask> getReservationEndExistingSubTasks(Reservation reservation);
}
