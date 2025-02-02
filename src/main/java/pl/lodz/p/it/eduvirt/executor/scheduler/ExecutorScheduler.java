package pl.lodz.p.it.eduvirt.executor.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;
import pl.lodz.p.it.eduvirt.executor.service.aggregate.ExecutorOperationService;
import pl.lodz.p.it.eduvirt.service.ReservationService;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"prod", "dev"})
@Transactional(propagation = Propagation.NEVER)
public class ExecutorScheduler {

    /* Operations */
    private final ExecutorOperationService executorOperationService;

    /* Model */
    private final ReservationService reservationService;
    private final ExecutorTaskService executorTaskService;

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0L)
    public void startReservations() {
        reservationService.findReservationsToBegin()
                .forEach(
                        reservation -> {
                            try {
                                executorOperationService.startUpPod(reservation);
                            } catch (Throwable e) {
                                log.error(
                                        "An error occurred during the task of creating the POD for reservation {}" +
                                                " ~ exception: {}: {} ",
                                        reservation.getId().toString(), e.getClass().getName(), e.getMessage()
                                );
                            }
                        }
                );
    }

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0L)
    public void stopReservations() {
        reservationService.findReservationsToStop()
                .forEach(
                        reservation -> {
                            try {
                                executorOperationService.stopPod(reservation);
                            } catch (Throwable e) {
                                log.error(
                                        "An error occurred during the task of destroying the POD for reservation {}" +
                                                " ~ exception: {}: {} ",
                                        reservation.getId().toString(), e.getClass().getName(), e.getMessage()
                                );
                            }
                        }
                );
    }

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0)
    public void endReservations() {
        executorTaskService.getReservationsToEndTasks()
                .forEach(
                        task -> {
                            try {
                                executorOperationService.finalizePodReservation(task);
                            } catch (Throwable e) {
                                log.error(
                                        "An error occurred during the task of ending the reservation {}" +
                                                " ~ exception: {}: {} ",
                                        task.getReservation().getId().toString(), e.getClass().getName(), e.getMessage()
                                );
                            }
                        }
                );
    }
}
