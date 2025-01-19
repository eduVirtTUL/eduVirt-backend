package pl.lodz.p.it.eduvirt.executor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.mails.MailNotification;
import pl.lodz.p.it.eduvirt.executor.repository.MailNotificationRepository;
import pl.lodz.p.it.eduvirt.executor.service.MailNotificationService;
import pl.lodz.p.it.eduvirt.util.MailProvider;

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MailNotificationServiceImpl implements MailNotificationService {

    private final MailProvider mailProvider;
    private final MailNotificationRepository mailNotificationRepository;

    @Override
    public void sendReservationStartNotification(Reservation reservation) {
        reservation.getTeam()
                .getUsers()
                .forEach(user ->
                        mailProvider.sendReservationStartEmail(
                                user.getFirstName(), user.getLastName(),
                                user.getEmail(),
                                reservation,
                                user.getTimeZone(),
                                user.getLanguage()
                        )
                );
        mailNotificationRepository.saveAndFlush(MailNotification.reservationStartNotification(reservation));
    }

    @Override
    public void sendReservationEndNotification(Reservation reservation) {
        //TODO michal: handle send mail retries if some error occurs
        //TODO michal: maybe add userId as constraint and register after each email send, to avoid sending many emails
        // to all students from team, if there is a problem with sending an email to one team member
        reservation.getTeam()
                .getUsers()
                .forEach(user ->
                        mailProvider.sendReservationEndEmail(
                                user.getFirstName(), user.getLastName(),
                                user.getEmail(),
                                reservation,
                                user.getTimeZone(),
                                user.getLanguage()
                        )
                );
        mailNotificationRepository.saveAndFlush(MailNotification.reservationEndNotification(reservation));
    }
}
