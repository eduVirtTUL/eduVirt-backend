package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.reservation.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.reservation.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.reservation.*;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.ReservationService;
import pl.lodz.p.it.eduvirt.util.BankerAlgorithm;
import pl.lodz.p.it.eduvirt.util.I18n;
import pl.lodz.p.it.eduvirt.util.MetricUtil;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    /* Services */

    private final OVirtClusterService clusterService;

    /* Repositories */

    private final ReservationRepository reservationRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;

    private final PodStatefulRepository podStatefulRepository;

    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;

    private final CourseMetricRepository courseMetricRepository;
    private final ClusterMetricRepository clusterMetricRepository;

    private final MaintenanceIntervalRepository maintenanceIntervalRepository;

    /* Util */

    private final MetricUtil metricUtil;
    private final BankerAlgorithm bankerAlgorithm;

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

        Cluster foundCluster = clusterService.findClusterById(foundCourse.getClusterId());

        // TODO: Replace it with actual id extraction
        String userStringId = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString(userStringId);
        Team foundTeam = teamRepository.findByUserIdAndCourse(userId, foundCourse)
                .orElseThrow(() -> new RuntimeException("Team containing user: %s not found".formatted(userId)));

        /* TODO: It would be very nice to simply it (as much as possible) */

        /* TODO: Check all the required conditions
         *        [V] Maximum reservation length
         *        [ ] Maximum number of reservations for given resource group
         *        [V] Grace period for next reservation of the same resource group
         *        [V] Required resource availability for course
         *        [V] Required resource availability for cluster
         *        [V] Resource group availability
         *        [V] Maintenance interval exists during selected time period
         *
         *        NOTES:
         *        1. Cond. 2: Resource group could be missing max. reservation count
         * */

        // General data validation

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (start.isBefore(currentTime)) throw new ReservationStartInPastException();
        if (end.isBefore(start)) throw new ReservationEndBeforeStartException();

        // Condition no. 1: Maximum reservation length

        int maxRentHours = foundResourceGroup.getMaxRentTime();
        int reservationLengthHours = (int) Duration.between(start, end).get(ChronoUnit.HOURS);
        ChronoUnit.HOURS.between(start, end);

        if (reservationLengthHours > maxRentHours)
            throw new RuntimeException("Reservation for resource group: %s could not be longer than: %d"
                    .formatted(foundResourceGroup.getId(), maxRentHours));

        // Condition no. 2: Maximum number of reservations for given resource group

        // TODO: This functionality is yet to be implemented

        // Condition no. 3: Grace period for previous reservation

        int gracePeriodInHours = foundPool.getGracePeriod();
        Optional<Reservation> lastReservation = reservationRepository.findLastReservation(foundResourceGroup, foundTeam);

        if (lastReservation.isPresent()) {
            LocalDateTime lastReservationEnd = lastReservation.get().getEndTime();
            int hoursBetweenReservations = (int) Duration.between(lastReservationEnd, start).get(ChronoUnit.HOURS);
            if (hoursBetweenReservations < gracePeriodInHours)
                throw new RuntimeException("Reservation grace period ends at: %s"
                        .formatted(lastReservationEnd.plusHours(gracePeriodInHours)));
        }

        // Condition no. 4: Resources availability for given course

        List<CourseMetric> foundCourseMetrics = courseMetricRepository.findAllByCourse(foundCourse);

        List<Reservation> foundCourseReservations = reservationRepository.findCurrentReservationsForCourse(
                foundCourse, OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());

        if (!bankerAlgorithm.process(() -> metricUtil.extractCourseMetricValues(foundCourseMetrics), foundCourseReservations, foundCluster))
            throw new CourseInsufficientResourcesException();

        // Condition no. 5: Resources availability for given cluster

        List<ClusterMetric> foundClusterMetrics = clusterMetricRepository
                .findAllByClusterId(foundCourse.getClusterId());

        List<Reservation> foundClusterReservations = reservationRepository.findCurrentReservationsForCluster(
                foundCourse.getClusterId(), OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());

        if (!bankerAlgorithm.process(() -> metricUtil.extractClusterMetricValues(foundClusterMetrics), foundClusterReservations, foundCluster))
            throw new ClusterInsufficientResourcesException();

        // Condition no. 6: Resource group availability

        List<Reservation> foundReservations = reservationRepository
                .findReservationForGivenPeriodForResourceGroup(foundResourceGroup, start, end);
        if (!foundReservations.isEmpty())
            throw new RuntimeException("Reservation for resource group: %s is already made"
                    .formatted(foundResourceGroup.getId()));

        // Condition no. 7: Maintenance intervals

        List<MaintenanceInterval> foundIntervals = maintenanceIntervalRepository
                .findAllIntervalsInGivenTimePeriod(foundCourse.getClusterId(), start, end);

        if (!foundIntervals.isEmpty())
            throw new ReservationCreationException(I18n.RESERVATION_MAINTENANCE_INTERVAL_CONFLICT);

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
    public List<Reservation> findCurrentReservationsForCluster(UUID clusterId, LocalDateTime currentTime) {
        return reservationRepository.findCurrentReservationsForCluster(clusterId, currentTime);
    }

    @Override
    public List<Reservation> findReservationsForGivenPeriod(UUID resourceGroupId, LocalDateTime start, LocalDateTime end) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(resourceGroupId));
        return reservationRepository.findReservationForGivenPeriodForResourceGroup(resourceGroup, start, end);
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
