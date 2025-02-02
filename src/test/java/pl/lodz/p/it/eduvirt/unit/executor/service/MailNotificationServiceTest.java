package pl.lodz.p.it.eduvirt.unit.executor.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.executor.entity.mails.MailNotification;
import pl.lodz.p.it.eduvirt.executor.repository.MailNotificationRepository;
import pl.lodz.p.it.eduvirt.executor.service.impl.MailNotificationServiceImpl;
import pl.lodz.p.it.eduvirt.util.MailProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MailNotificationServiceTest {

    @Mock
    private MailProvider mailProvider;

    @Mock
    private MailNotificationRepository mailNotificationRepository;

    @InjectMocks
    private MailNotificationServiceImpl mailNotificationService;

    /* Test data */

    private Reservation testReservation;
    private Team testTeam;
    private User testUser_1;
    private User testUser_2;

    /* Data initialization */

    @BeforeEach
    void setUp() {
        testUser_1 =  new User(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testUser_1_Mail",
                "testUser_1_UserName",
                "testUser_1_Firstname",
                "testUser_1_Lastname"
        );
        testUser_1.setTimeZone("CET");
        testUser_1.setLanguage("pl");

        testUser_2 = new User(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "testUser_2_Mail",
                "testUser_2_UserName",
                "testUser_2_Firstname",
                "testUser_2_Lastname"
        );
        testUser_2.setTimeZone("CET");
        testUser_2.setLanguage("pl");

        testTeam = new Team();
        testTeam.setUsers(List.of(testUser_1, testUser_2));

        testReservation = new Reservation();
        testReservation.setTeam(testTeam);

        setEntityId(testReservation, UUID.randomUUID());
    }

    /* Tests */

    @Test
    void Given_Reservation_When_SendReservationStartNotification_Then_Success() {
        doNothing().when(mailProvider).sendReservationStartEmail(
                any(String.class),
                any(String.class),
                any(String.class),
                eq(testReservation),
                any(String.class),
                any(String.class)
        );

        when(mailNotificationRepository.saveAndFlush(any(MailNotification.class)))
                .thenReturn(new MailNotification(testReservation, MailNotification.NotificationType.RESERVATION_START));

        mailNotificationService.sendReservationStartNotification(testReservation);

        verify(mailProvider, times(2)).sendReservationStartEmail(
                any(String.class),
                any(String.class),
                any(String.class),
                eq(testReservation),
                any(String.class),
                any(String.class)
        );
        verify(mailNotificationRepository, times(1)).saveAndFlush(any(MailNotification.class));
    }

    @Test
    void Given_Reservation_When_SendReservationEndNotification_Then_Success() {
        doNothing().when(mailProvider).sendReservationEndEmail(
                any(String.class),
                any(String.class),
                any(String.class),
                eq(testReservation),
                any(String.class),
                any(String.class)
        );

        when(mailNotificationRepository.saveAndFlush(any(MailNotification.class)))
                .thenReturn(new MailNotification(testReservation, MailNotification.NotificationType.RESERVATION_START));

        mailNotificationService.sendReservationEndNotification(testReservation);

        verify(mailProvider, times(2)).sendReservationEndEmail(
                any(String.class),
                any(String.class),
                any(String.class),
                eq(testReservation),
                any(String.class),
                any(String.class)
        );
        verify(mailNotificationRepository, times(1)).saveAndFlush(any(MailNotification.class));
    }

    @SneakyThrows
    private void setEntityId(AbstractEntity entity, UUID id) {
        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        idField.setAccessible(false);
    }
}
