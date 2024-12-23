package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.exceptions.reservation.ReservationEndBeforeStartException;
import pl.lodz.p.it.eduvirt.exceptions.reservation.ReservationStartInPastException;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.service.ReservationService;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;

    @Override
    public void createReservation(UUID resourceGroupId, LocalDateTime start, LocalDateTime end, boolean automaticStartup) {
        ResourceGroup foundResourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new RuntimeException("Resource group with id: %s not found".formatted(resourceGroupId)));

        ResourceGroupPool foundPool = resourceGroupPoolRepository
                .getResourceGroupPoolByResourceGroupsContaining(foundResourceGroup)
                .orElseThrow(() -> new RuntimeException("Resource group pool containing resource group: %s not found".formatted(resourceGroupId)));

        Course foundCourse = courseRepository
                .findByResourceGroupPoolsContaining(foundPool)
                .orElseThrow(() -> new RuntimeException("Course containing resource group pool: %s not found".formatted(foundResourceGroup.getId())));

        String userStringId = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString(userStringId);
        Team foundTeam = teamRepository.findByUserIdAndCourse(userId, foundCourse)
                .orElseThrow(() -> new RuntimeException("Team containing user: %s not found".formatted(userId)));

        /* TODO: It would be very nice to simply it (as much as possible) */

        /* TODO: Check all the required conditions
        *        [V] Maximum reservation length
        *        [ ] Maximum number of reservations for given resource group
        *        [V] Grace period for next reservation of the same resource group
        *        [ ] Required resource availability for course
        *        [ ] Required resource availability for cluster
        *        [V] Resource group availability
        *        [ ] Maintenance interval exists during selected time period
        *
        *        NOTES:
        *        1. Cond. 2: Resource group could be missing max. reservation count
        *        2. 4th and 5th check cannot be done without metrics for course (and connecting metrics to resources)
        * */

        // General data validation

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (start.isBefore(currentTime)) throw new ReservationStartInPastException();
        if (end.isBefore(start)) throw new ReservationEndBeforeStartException();

        // Condition no. 1: Maximum reservation length

        int maxRentHours = foundResourceGroup.getMaxRentTime();
        int reservationLengthHours = (int) Duration.between(start, end).get(ChronoUnit.HOURS);

        if (reservationLengthHours > maxRentHours)
            throw new RuntimeException("Reservation for resource group: %s could not be longer than: %d"
                    .formatted(foundResourceGroup.getId(), maxRentHours));

        // Condition no. 2: Maximum number of reservations for given resource group

        // Condition no. 3: Grace period for previous reservation

        int gracePeriodInHours = foundPool.getGracePeriod();
        Optional<Reservation> lastReservation = reservationRepository.findLastReservation(foundResourceGroup, foundTeam);

        if (lastReservation.isPresent()) {
            LocalDateTime lastReservationEnd = lastReservation.get().getEndTime();
            int hoursBetweenReservations = (int) Duration.between(lastReservationEnd, start).get(ChronoUnit.HOURS);
            if (hoursBetweenReservations < gracePeriodInHours) throw new RuntimeException("Reservation grace period ends at: %s"
                    .formatted(lastReservationEnd.plusHours(gracePeriodInHours)));
        }

        // Condition no. 4: Resources availability for given course

        // Condition no. 5: Resources availability for given cluster

        // Condition no. 6: Resource group availability

        List<Reservation> foundReservations = reservationRepository.findReservationForGivenPeriod(start, end);
        if (!foundReservations.isEmpty()) throw new RuntimeException("Reservation for resource group: %s is already made"
                .formatted(foundResourceGroup.getId()));

        /* TODO: Condition check end */

        Reservation newReservation = new Reservation(
                foundResourceGroup,
                foundTeam,
                start,
                end,
                automaticStartup
        );

        reservationRepository.saveAndFlush(newReservation);
    }

    @Override
    public Optional<Reservation> findReservationById(UUID reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @Override
    public List<Reservation> findCurrentReservationsForCourse(Course course, LocalDateTime currentTime) {
        return reservationRepository.findCurrentReservationsForCourse(course, currentTime);
    }

    @Override
    public List<Reservation> findReservationsForGivenPeriod(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findReservationForGivenPeriod(start, end);
    }

    @Override
    public void finishReservation(Reservation reservation) {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (reservation.getStartTime().isBefore(currentTime)) {
            reservation.setEndTime(currentTime);
            reservationRepository.saveAndFlush(reservation);
        } else {
            reservationRepository.delete(reservation);
        }
    }
}
