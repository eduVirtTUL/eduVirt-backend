package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ReservationService {

    /* Create methods */

    void createReservationForStatefulPod(Team team, PodStateful podId, CreateReservationDto createDto);

    // TODO: Change after stateless pod is completed.
    void createReservationForStatelessPod(Team team, UUID podId, CreateReservationDto createDto);

    /* Read methods */

    Optional<Reservation> findReservationById(UUID reservationId);

    List<Reservation> findRgReservations(ResourceGroup resourceGroup, Course course, LocalDateTime start, LocalDateTime end);
    List<Reservation> findRgPoolReservations(ResourceGroupPool resourceGroupPool, Course course, LocalDateTime start, LocalDateTime end);

    Page<Reservation> findReservationsForStatelessPod(UUID statelessPod, Team team, Pageable pageable);
    Page<Reservation> findReservationsForStatefulPod(PodStateful statefulPod, Team team, Pageable pageable);

    Page<Reservation> findActiveReservations(UUID teamId, Pageable pageable);
    Page<Reservation> findHistoricalReservations(UUID teamId, Pageable pageable);

    Map<LocalDateTime, Boolean> checkResourceGroupAvailability(
            ResourceGroup resourceGroup, Course course,
            int windowLength, LocalDateTime start, LocalDateTime end);

    Map<LocalDateTime, Boolean> checkResourceGroupPoolAvailability(
            ResourceGroupPool resourceGroupPool, Course course,
            int windowLength, LocalDateTime start, LocalDateTime end);

    /* Update / delete methods */

    void finishReservation(Reservation reservation);

    void startReservation(Reservation reservation);
    void endReservation(Reservation reservation);
}
