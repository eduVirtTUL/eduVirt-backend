package pl.lodz.p.it.eduvirt.executor.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.executor.service.MailNotificationService;
import pl.lodz.p.it.eduvirt.service.ReservationService;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Profile({"prod", "dev"})
@Transactional(propagation = Propagation.NEVER)
public class NotificationScheduler {

    private final ReservationService reservationService;
    private final MailNotificationService mailNotificationService;

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0L)
    @Transactional(propagation = Propagation.NEVER)
    public void sendNotifications() {
        reservationService.findReservationsToSendNotifications()
                .forEach(
                        reservation -> {
                            try {
                                mailNotificationService.sendReservationEndNotification(reservation);
                            } catch (Throwable e) {
                                log.error(
                                        "An error occurred during the task of ending the reservation {}" +
                                                " ~ exception: {}: {} ",
                                        reservation.getId().toString(), e.getClass().getName(), e.getMessage()
                                );
                            }
                        }
                );
    }
}
