package pl.lodz.p.it.eduvirt.executor.service;

import pl.lodz.p.it.eduvirt.entity.Reservation;

public interface MailNotificationService {

    void sendReservationStartNotification(Reservation reservation);

    void sendReservationEndNotification(Reservation reservation);
}
