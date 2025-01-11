package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Host;
import org.ovirt.engine.sdk4.types.Qos;
import org.ovirt.engine.sdk4.types.Vm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
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
@Transactional(propagation = Propagation.REQUIRED)
public class ReservationServiceImpl implements ReservationService {

    /* Services */

    private final OVirtClusterService clusterService;
    private final OVirtVmService vmService;

    /* Repositories */

    private final ReservationRepository reservationRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;

    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;

    private final CourseMetricRepository courseMetricRepository;
    private final ClusterMetricRepository clusterMetricRepository;

    private final MaintenanceIntervalRepository maintenanceIntervalRepository;

    /* Util */

    private final MetricUtil metricUtil;
    private final BankerAlgorithm bankerAlgorithm;

    /* Create methods */

    @PreAuthorize("hasRole('student')")
    @Override
    public void createReservation(UUID resourceGroupId, LocalDateTime start, LocalDateTime end,
                                  boolean automaticStartup, int notificationTime) {
        ResourceGroup foundResourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(resourceGroupId));

        ResourceGroupPool foundPool = resourceGroupPoolRepository
                .getResourceGroupPoolByResourceGroupsContaining(foundResourceGroup)
                .orElseThrow(() -> new ResourceGroupPoolNotFoundException(
                        "Resource group pool containing resource group: %s not found".formatted(resourceGroupId)));

        Course foundCourse = courseRepository
                .findByResourceGroupPoolsContaining(foundPool)
                .orElseThrow(() -> new CourseNotFoundException(
                        "Course containing resource group pool: %s not found".formatted(foundResourceGroup.getId())));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        Team foundTeam = teamRepository.findByUserIdAndCourse(userId, foundCourse)
                .orElseThrow(() -> new TeamNotFoundException("Team containing user: %s not found".formatted(userId)));

        Cluster foundCluster = clusterService.findClusterById(foundCourse.getClusterId());
        List<Host> foundHosts = clusterService.findAllHostsInCluster(foundCluster);

        Map<String, Vm> foundVms = new HashMap<>();
        Map<String, Qos> foundQos = new HashMap<>();
        for (Vm vm : vmService.findVmsForCluster(foundCluster)) {
            foundVms.put(vm.id(), vm);
            if (vm.cpuProfile().qos() != null) {
                foundQos.put(vm.id(), vmService.findQosForVmCpu(vm));
            }
        }

        /* TODO: It would be very nice to simplify it (as much as possible) */

        /* TODO: Check all the required conditions
         *        [V] Minimum reservation length (that is 1 hour)
         *        [V] Maximum reservation length
         *        [ ] Maximum number of reservations for given resource group
         *        [V] Grace period for next reservation of the same resource group
         *        [V] Required resource availability for course
         *        [V] Required resource availability for cluster
         *        [V] Resource group availability
         *        [V] Maintenance interval exists during selected time period
         *
         *        NOTES:
         *        1. Cond. 3: Resource group could be missing max. reservation count
         * */

        // General data validation

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (start.isBefore(currentTime)) throw new ReservationStartInPastException();
        if (end.isBefore(start)) throw new ReservationEndBeforeStartException();

        // Condition no. 1: Maximum reservation length

        if ((int) ChronoUnit.HOURS.between(start, end) < 1)
            throw new ReservationTooShortException(
                    "Minimum length of the reservation in eduVirt system is exactly 1 hour.");

        // Condition no. 2: Maximum reservation length

        int maxRentHours = foundResourceGroup.getMaxRentTime();
        int reservationLengthHours = (int) ChronoUnit.HOURS.between(start, end);

        if (reservationLengthHours > maxRentHours)
            throw new ReservationMaxLengthExceededException("Reservation for resource group: %s could not be longer than: %d"
                    .formatted(foundResourceGroup.getId(), maxRentHours));

        // Condition no. 3: Maximum number of reservations for given resource group

        // TODO: This functionality is yet to be implemented

        // Condition no. 4: Grace period for previous reservation

        int gracePeriodInHours = foundPool.getGracePeriod();
        List<Reservation> reservationsBefore = reservationRepository.findResourceGroupReservationForGivenTeamInTimePeriod(
                foundResourceGroup, foundTeam, start.minusHours(gracePeriodInHours), start);

        List<Reservation> reservationsAfter = reservationRepository.findResourceGroupReservationForGivenTeamInTimePeriod(
                foundResourceGroup, foundTeam, end, end.plusHours(gracePeriodInHours));

        if (!reservationsBefore.isEmpty())
            throw new ReservationGracePeriodNotFinishedException(
                    "Reservation grace period, which is %d hours, will not be finished before scheduled reservation."
                    .formatted(gracePeriodInHours));

        if (!reservationsAfter.isEmpty())
            throw new ReservationGracePeriodCouldNotFinishException(
                    "Reservation grace period, which is %d hours, will not be finished before next reservation."
                            .formatted(gracePeriodInHours));

        // Condition no. 5: Resources availability for given course

        List<CourseMetric> foundCourseMetrics = courseMetricRepository.findAllByCourse(foundCourse);

        List<Reservation> foundCourseReservations = reservationRepository.findCurrentReservationsForCourse(
                foundCourse, start, end);

        if (!bankerAlgorithm.process(() -> metricUtil.extractCourseMetricValues(foundCourseMetrics),
                foundCourseReservations, foundResourceGroup, foundCluster, foundHosts, foundVms, foundQos))
            throw new CourseInsufficientResourcesException(foundCourse.getId());

        // Condition no. 6: Resources availability for given cluster

        List<ClusterMetric> foundClusterMetrics = clusterMetricRepository
                .findAllByClusterId(foundCourse.getClusterId());

        List<Reservation> foundClusterReservations = reservationRepository.findCurrentReservationsForCluster(
                foundCourse.getClusterId(), start, end);

        if (!bankerAlgorithm.process(() -> metricUtil.extractClusterMetricValues(foundClusterMetrics),
                foundClusterReservations, foundResourceGroup, foundCluster, foundHosts, foundVms, foundQos))
            throw new ClusterInsufficientResourcesException(UUID.fromString(foundCluster.id()));

        // Condition no. 7: Resource group availability

        List<Reservation> foundReservations = reservationRepository
                .findReservationForGivenPeriodForResourceGroup(foundResourceGroup, start, end);
        if (!foundReservations.isEmpty())
            throw new ResourceGroupAlreadyReservedException("Reservation for resource group: %s is already made"
                    .formatted(foundResourceGroup.getId()));

        // Condition no. 8: Maintenance intervals

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
                automaticStartup,
                notificationTime
        );

        reservationRepository.saveAndFlush(newReservation);
    }

    /* Read methods */

    @PreAuthorize("isAuthenticated()")
    @Override
    public Optional<Reservation> findReservationById(UUID reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<Reservation> findCurrentReservationsForCourse(Course course, LocalDateTime start, LocalDateTime end) {
        // TODO: Add delete logic dependent on access level
        return reservationRepository.findCurrentReservationsForCourse(course, start, end);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<Reservation> findCurrentReservationsForCluster(UUID clusterId, LocalDateTime start, LocalDateTime end) {
        // TODO: Add delete logic dependent on access level
        return reservationRepository.findCurrentReservationsForCluster(clusterId, start, end);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<Reservation> findReservationsForGivenPeriod(UUID resourceGroupId, LocalDateTime start, LocalDateTime end) {
        // TODO: Add delete logic dependent on access level
        ResourceGroup resourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(resourceGroupId));
        return reservationRepository.findReservationForGivenPeriodForResourceGroup(resourceGroup, start, end);
    }

    @PreAuthorize("hasAnyRole('student')")
    @Override
    public Page<Reservation> findActiveReservations(UUID userId, UUID courseId, Pageable pageable) {
        // TODO: Add delete logic dependent on access level
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        Team foundTeam = teamRepository.findByUserIdAndCourse(userId, course)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team containing user %s in course %s could not be found".formatted(userId, courseId)));

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        return reservationRepository.findAllActiveReservations(foundTeam, currentTime, pageable);
    }

    @PreAuthorize("hasAnyRole('student')")
    @Override
    public Page<Reservation> findHistoricalReservations(UUID userId, UUID courseId, Pageable pageable) {
        // TODO: Add delete logic dependent on access level
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        Team foundTeam = teamRepository.findByUserIdAndCourse(userId, course)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team containing user %s in course %s could not be found".formatted(userId, courseId)));

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        return reservationRepository.findAllHistoricalReservations(foundTeam, currentTime, pageable);
    }

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Override
    public Page<Reservation> findActiveReservations(UUID teamId, Pageable pageable) {
        // TODO: Add delete logic dependent on access level
        Team foundTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        return reservationRepository.findAllHistoricalReservations(foundTeam, currentTime, pageable);
    }

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Override
    public Page<Reservation> findHistoricalReservations(UUID teamId, Pageable pageable) {
        // TODO: Add delete logic dependent on access level
        Team foundTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        return reservationRepository.findAllHistoricalReservations(foundTeam, currentTime, pageable);
    }

    /* Update / delete methods */

    @PreAuthorize("isAuthenticated()")
    @Override
    public void finishReservation(Reservation reservation) {
        // TODO: Add delete logic dependent on access level
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (reservation.getStartTime().isBefore(currentTime)) {
            reservation.setEndTime(currentTime);
            reservationRepository.saveAndFlush(reservation);
        } else {
            reservationRepository.delete(reservation);
        }
    }

    @PreAuthorize("permitAll()")
    @Override
    public void startReservation(Reservation reservation) {
        if (reservation.getStatus().equals(Reservation.ReservationStatus.IN_PROGRESS))
            throw new ReservationStatusException(
                    "Reservation %s has been started already.".formatted(reservation.getId()));

        if (reservation.getStatus().equals(Reservation.ReservationStatus.COMPLETED))
            throw new ReservationStatusException(
                    "Reservation %s has been completed already.".formatted(reservation.getId()));

        reservation.setStatus(Reservation.ReservationStatus.IN_PROGRESS);
        reservationRepository.saveAndFlush(reservation);
    }

    @PreAuthorize("permitAll()")
    @Override
    public void endReservation(Reservation reservation) {
        if (reservation.getStatus().equals(Reservation.ReservationStatus.PENDING))
            throw new ReservationStatusException(
                    "Reservation %s has not started yet.".formatted(reservation.getId()));

        if (reservation.getStatus().equals(Reservation.ReservationStatus.COMPLETED))
            throw new ReservationStatusException(
                    "Reservation %s has been completed already.".formatted(reservation.getId()));

        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservationRepository.saveAndFlush(reservation);
    }
}
