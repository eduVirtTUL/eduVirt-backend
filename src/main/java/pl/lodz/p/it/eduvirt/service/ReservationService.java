package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationService {

    /* Create methods */

    void createReservation(UUID resourceGroupId, LocalDateTime start, LocalDateTime end,
                           boolean automaticStartup, int notificationTime);

    /* Read methods */

    Optional<Reservation> findReservationById(UUID reservationId);

    List<Reservation> findCurrentReservationsForCourse(Course course, LocalDateTime start, LocalDateTime end);
    List<Reservation> findCurrentReservationsForCluster(UUID clusterId, LocalDateTime start, LocalDateTime end);
    List<Reservation> findReservationsForGivenPeriod(UUID resourceGroupId, LocalDateTime start, LocalDateTime end);

    Page<Reservation> findActiveReservations(UUID userId, UUID courseId, Pageable pageable);
    Page<Reservation> findHistoricalReservations(UUID userId, UUID courseId, Pageable pageable);
    Page<Reservation> findActiveReservations(UUID teamId, Pageable pageable);
    Page<Reservation> findHistoricalReservations(UUID teamId, Pageable pageable);

    /* Update / delete methods */

    void finishReservation(Reservation reservation);

    void startReservation(Reservation reservation);
    void endReservation(Reservation reservation);
}
