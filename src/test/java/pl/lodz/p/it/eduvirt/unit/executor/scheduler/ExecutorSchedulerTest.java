package pl.lodz.p.it.eduvirt.unit.executor.scheduler;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.scheduler.ExecutorScheduler;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;
import pl.lodz.p.it.eduvirt.executor.service.aggregate.ExecutorOperationService;
import pl.lodz.p.it.eduvirt.service.ReservationService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExecutorSchedulerTest {

    @Mock
    private ExecutorOperationService executorOperationService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private ExecutorTaskService executorTaskService;

    @InjectMocks
    private ExecutorScheduler executorScheduler;

    /* Test data */

    private Reservation testReservation1;
    private Reservation testReservation2;

    private ExecutorTask testExecutorTask1;
    private ExecutorTask testExecutorTask2;

    /* Data initialization */

    @BeforeEach
    void setUp() {
        testReservation1 = new Reservation();
        setEntityId(testReservation1, UUID.randomUUID());

        testReservation2 = new Reservation();
        setEntityId(testReservation2, UUID.randomUUID());

        testExecutorTask1 = new ExecutorTask(testReservation1, null);
        setEntityId(testExecutorTask1, UUID.randomUUID());

        testExecutorTask2 = new ExecutorTask(testReservation1, null);
        setEntityId(testExecutorTask2, UUID.randomUUID());
    }

    /* Tests */

    @Test
    void Given_Reservations_When_StartReservations_Then_Success() {
        when(reservationService.findReservationsToBegin())
                .thenReturn(List.of(testReservation1, testReservation2));

        doNothing().when(executorOperationService).startUpPod(any(Reservation.class));

        executorScheduler.startReservations();

        verify(executorOperationService, times(2)).startUpPod(any(Reservation.class));
        verify(executorOperationService, times(1)).startUpPod(testReservation1);
        verify(executorOperationService, times(1)).startUpPod(testReservation2);
    }

    @Test
    void Given_ReservationsAndFirstFailedBudSecondSucceeded_When_StartReservations_Then_Success() {
        when(reservationService.findReservationsToBegin())
                .thenReturn(List.of(testReservation1, testReservation2));

        doThrow(RuntimeException.class).when(executorOperationService).startUpPod(testReservation1);
        doNothing().when(executorOperationService).startUpPod(testReservation2);

        executorScheduler.startReservations();

        verify(executorOperationService, times(2)).startUpPod(any(Reservation.class));
        verify(executorOperationService, times(1)).startUpPod(testReservation1);
        verify(executorOperationService, times(1)).startUpPod(testReservation2);
    }

    @Test
    void Given_Reservations_When_StopReservations_Then_Success() {
        when(reservationService.findReservationsToStop())
                .thenReturn(List.of(testReservation1, testReservation2));

        doNothing().when(executorOperationService).stopPod(any(Reservation.class));

        executorScheduler.stopReservations();

        verify(executorOperationService, times(2)).stopPod(any(Reservation.class));
        verify(executorOperationService, times(1)).stopPod(testReservation1);
        verify(executorOperationService, times(1)).stopPod(testReservation2);
    }

    @Test
    void Given_ReservationsAndFirstFailedBudSecondSucceeded_When_StopReservations_Then_Success() {
        when(reservationService.findReservationsToStop())
                .thenReturn(List.of(testReservation1, testReservation2));

        doThrow(RuntimeException.class).when(executorOperationService).stopPod(testReservation1);
        doNothing().when(executorOperationService).stopPod(testReservation2);

        executorScheduler.stopReservations();

        verify(executorOperationService, times(2)).stopPod(any(Reservation.class));
        verify(executorOperationService, times(1)).stopPod(testReservation1);
        verify(executorOperationService, times(1)).stopPod(testReservation2);
    }

    @Test
    void Given_Reservations_When_EndReservations_Then_Success() {
        when(executorTaskService.getReservationsToEndTasks())
                .thenReturn(List.of(testExecutorTask1, testExecutorTask2));

        doNothing().when(executorOperationService).finalizePodReservation(any(ExecutorTask.class));

        executorScheduler.endReservations();

        verify(executorOperationService, times(2)).finalizePodReservation(any(ExecutorTask.class));
        verify(executorOperationService, times(1)).finalizePodReservation(testExecutorTask1);
        verify(executorOperationService, times(1)).finalizePodReservation(testExecutorTask2);
    }

    @Test
    void Given_ReservationsAndFirstFailedBudSecondSucceeded_When_EndReservations_Then_Success() {
        when(executorTaskService.getReservationsToEndTasks())
                .thenReturn(List.of(testExecutorTask1, testExecutorTask2));

        doThrow(RuntimeException.class).when(executorOperationService).finalizePodReservation(testExecutorTask1);
        doNothing().when(executorOperationService).finalizePodReservation(testExecutorTask2);

        executorScheduler.endReservations();

        verify(executorOperationService, times(2)).finalizePodReservation(any(ExecutorTask.class));
        verify(executorOperationService, times(1)).finalizePodReservation(testExecutorTask1);
        verify(executorOperationService, times(1)).finalizePodReservation(testExecutorTask2);
    }

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
    }
}
