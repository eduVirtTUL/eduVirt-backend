package pl.lodz.p.it.eduvirt.executor.service.aggregate;

import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;

public interface ExecutorOperationService {

    void startUpPod(Reservation reservation);

    void stopPod(Reservation reservation);

    void finalizePodReservation(ExecutorTask task);
}
