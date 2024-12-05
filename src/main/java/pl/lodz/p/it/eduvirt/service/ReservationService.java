package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.eduvirt.Course;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationService {

    void createReservation(UUID resourceGroupId, LocalDateTime start, LocalDateTime end, boolean automaticStartup);

    Optional<Reservation> findReservationById(UUID reservationId);

    List<Reservation> findCurrentReservationsForCourse(Course course, LocalDateTime currentTime);
    List<Reservation> findReservationsForGivenPeriod(LocalDateTime start, LocalDateTime end);

    void cancelReservation(Reservation reservation);
}
