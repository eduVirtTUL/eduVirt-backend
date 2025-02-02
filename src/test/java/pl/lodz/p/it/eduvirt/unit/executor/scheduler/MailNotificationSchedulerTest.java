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
import pl.lodz.p.it.eduvirt.executor.scheduler.NotificationScheduler;
import pl.lodz.p.it.eduvirt.executor.service.MailNotificationService;
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
public class MailNotificationSchedulerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private MailNotificationService mailNotificationService;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    /* Test data */

    private Reservation testReservation1;
    private Reservation testReservation2;

    /* Data initialization */

    @BeforeEach
    void setUp() {
        testReservation1 = new Reservation();
        setEntityId(testReservation1, UUID.randomUUID());

        testReservation2 = new Reservation();
        setEntityId(testReservation2, UUID.randomUUID());
    }

    /* Tests */

    @Test
    void Given_Reservations_When_SendNotifications_Then_Success() {
        when(reservationService.findReservationsToSendNotifications())
                .thenReturn(List.of(testReservation1, testReservation2));

        doNothing().when(mailNotificationService).sendReservationEndNotification(any(Reservation.class));

        notificationScheduler.sendNotifications();

        verify(mailNotificationService, times(2)).sendReservationEndNotification(any(Reservation.class));
        verify(mailNotificationService, times(1)).sendReservationEndNotification(testReservation1);
        verify(mailNotificationService, times(1)).sendReservationEndNotification(testReservation2);
    }

    @Test
    void Given_ReservationsAndFirstFailedBudSecondSucceeded_When_SendNotifications_Then_Success() {
        when(reservationService.findReservationsToSendNotifications())
                .thenReturn(List.of(testReservation1, testReservation2));

        doThrow(RuntimeException.class).when(mailNotificationService).sendReservationEndNotification(testReservation1);
        doNothing().when(mailNotificationService).sendReservationEndNotification(testReservation2);

        notificationScheduler.sendNotifications();

        verify(mailNotificationService, times(2)).sendReservationEndNotification(any(Reservation.class));
        verify(mailNotificationService, times(1)).sendReservationEndNotification(testReservation1);
        verify(mailNotificationService, times(1)).sendReservationEndNotification(testReservation2);
    }


    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
    }
}
