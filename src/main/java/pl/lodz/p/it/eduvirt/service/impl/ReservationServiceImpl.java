package pl.lodz.p.it.eduvirt.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Host;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotFoundException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.ReservationService;
import pl.lodz.p.it.eduvirt.util.BankerAlgorithm;
import pl.lodz.p.it.eduvirt.util.I18n;
import pl.lodz.p.it.eduvirt.util.MailProvider;
import pl.lodz.p.it.eduvirt.util.MetricUtil;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class ReservationServiceImpl implements ReservationService {

    @Value("${resource.warning.mail}")
    private boolean resourcesWarningMails;

    @Value("${window.length}")
    private int windowLength;

    @Value("${executor.task-time-tolerance}")
    private int taskTimeTolerance;

    @Value("${executor.vm.grace-time}")
    private int vmGraceTime;

    @PostConstruct
    public void validateProperty() {
        if (windowLength < 10) windowLength = 10;
        if (windowLength > 60) windowLength = 60;
    }

    /* Services */

    private final OVirtClusterService clusterService;

    /* Repositories */

    private final ReservationRepository reservationRepository;
    private final TeamRepository teamRepository;
    private final CourseMetricRepository courseMetricRepository;
    private final ClusterMetricRepository clusterMetricRepository;
    private final MaintenanceIntervalRepository maintenanceIntervalRepository;
    private final UserRepository userRepository;

    /* Util */

    private final MetricUtil metricUtil;
    private final BankerAlgorithm bankerAlgorithm;
    private final MailProvider mailProvider;

    /* Create methods */

    @PreAuthorize("isAuthenticated()")
    @Override
    public void createReservationForStatefulPod(Team team, PodStateful pod, CreateReservationDto createDto) {
        ResourceGroup resourceGroup = pod.getResourceGroup();
        Course course = pod.getCourse();

        Cluster courseCluster = clusterService.findClusterById(course.getClusterId());
        List<Host> clusterHosts = clusterService.findAllHostsInCluster(courseCluster);

        // TODO: Uncomment after the stateless pod is done
        /* TODO: Check all the required conditions
         *        [V] Minimum reservation length (that is 2 * window length)
         *        [V] Maximum reservation length
         *        [V] Maximum number of reservations for given resource group
         *        [V] Maintenance interval exists during selected time period
         *        [V] Resource group availability
         *        [V] Required resource availability for course
         *        [V] Required resource availability for cluster
         * */

        /* [V]  General data validation */

        LocalDateTime start = createDto.start();
        LocalDateTime end = createDto.end();
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();

        if (start.isBefore(currentTime)) throw new ReservationStartInPastException();
        if (end.isBefore(start)) throw new ReservationEndBeforeStartException();

        /* Limit reservation length to the multiplicity of window length */

        long numOfIntervals = (ChronoUnit.SECONDS.between(start, end) / TimeUnit.MINUTES.toSeconds(windowLength));
        end = start.plusMinutes(numOfIntervals * windowLength);

        long reservationLength = ChronoUnit.SECONDS.between(start, end);
        if (TimeUnit.MINUTES.toSeconds(createDto.notificationTime()) > (reservationLength / 2))
            throw new ReservationNotificationTimeTooLongException("Notification time could not be longer than half the reservation duration.");

        /* Condition no. 1: Minimum reservation length */

        if (reservationLength < (2L * TimeUnit.MINUTES.toSeconds(windowLength)))
            throw new ReservationTooShortException("Minimum length of the reservation in eduVirt system is exactly twice as long as assumed window length.");

        /* Condition no. 2: Maximum reservation length */

        long maxRentTime = TimeUnit.HOURS.toSeconds(resourceGroup.getMaxRentTime());
        if (maxRentTime != 0 && reservationLength > maxRentTime)
            throw new ReservationMaxLengthExceededException("Reservation for resource group: %s could not be longer than: %d"
                    .formatted(resourceGroup.getId(), maxRentTime));

        /* Condition no. 3: Maximum number of reservations for given resource group */

        int reservationLimit = pod.getMaxRent();
        List<Reservation> rgTeamReservations = reservationRepository
                .findAllRgReservationsForGivenTeam(resourceGroup, team);

        if (reservationLimit != 0 && rgTeamReservations.size() >= reservationLimit)
            throw new ResourceGroupReservationCountExceededException(
                    "Team %s has already made all available reservations for resource group: %s"
                            .formatted(team.getId(), resourceGroup.getId()));

        /* Condition no. 4: Maintenance intervals */

        List<MaintenanceInterval> foundIntervals = maintenanceIntervalRepository
                .findAllIntervalsInGivenTimePeriod(course.getClusterId(), start, end);

        if (!foundIntervals.isEmpty())
            throw new ReservationCreationException(I18n.RESERVATION_MAINTENANCE_INTERVAL_CONFLICT);

        /* Condition no. 5: Resource group availability */

        List<Reservation> foundReservations = reservationRepository
                .findRgReservations(resourceGroup, start, end);
        if (!foundReservations.isEmpty())
            throw new ResourceGroupAlreadyReservedException("Reservation for resource group: %s is already made"
                    .formatted(resourceGroup.getId()));

        /* Condition no. 6: Resources availability for given course */

        List<CourseMetric> foundCourseMetrics = courseMetricRepository.findAllByCourse(course);
        List<Reservation> foundCourseReservations = reservationRepository
                .findCourseReservations(course, start, end);

        if (!bankerAlgorithm.process(() -> metricUtil.extractCourseMetricValues(foundCourseMetrics),
                foundCourseReservations, resourceGroup, courseCluster, clusterHosts))
            throw new CourseInsufficientResourcesException(course.getId());

        /* Condition no. 7: Resources availability for given cluster */

        List<ClusterMetric> foundClusterMetrics = clusterMetricRepository.findAllByClusterId(course.getClusterId());
        List<Reservation> foundClusterReservations = reservationRepository
                .findClusterReservations(course.getClusterId(), start, end);

        if (!bankerAlgorithm.process(() -> metricUtil.extractClusterMetricValues(foundClusterMetrics),
                foundClusterReservations, resourceGroup, courseCluster, clusterHosts))
            throw new ClusterInsufficientResourcesException(UUID.fromString(courseCluster.id()));

        /* TODO: Condition check end */

        Reservation newReservation = new Reservation(
                resourceGroup, team, start, end,
                createDto.automaticStartup(),
                createDto.notificationTime()
        );

        reservationRepository.saveAndFlush(newReservation);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public void createReservationForStatelessPod(Team team, PodStateless pod, CreateReservationDto createDto) {
        ResourceGroupPool resourceGroupPool = pod.getResourceGroupPool();
        Course course = pod.getCourse();

        Cluster courseCluster = clusterService.findClusterById(course.getClusterId());
        List<Host> clusterHosts = clusterService.findAllHostsInCluster(courseCluster);

        // TODO: Finish implementing when stateless pod is done
        /* TODO: Check all the required conditions
         *        [V] Minimum reservation length (that is 2 * window length)
         *        [V] Maximum reservation length
         *        [V] Maximum number of reservations for given resource group
         *        [V] Grace period for next reservation of the same resource group
         *        [V] Required resource availability for course
         *        [V] Required resource availability for cluster
         *        [V] Resource group availability
         *        [V] Maintenance interval exists during selected time period
         * */

        /* [V]  General data validation */

        LocalDateTime start = createDto.start();
        LocalDateTime end = createDto.end();

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();

        if (start.isBefore(currentTime)) throw new ReservationStartInPastException();
        if (end.isBefore(start)) throw new ReservationEndBeforeStartException();

        /* Limit reservation length to the multiplicity of window length */

        long numOfIntervals = (ChronoUnit.SECONDS.between(start, end) / TimeUnit.MINUTES.toSeconds(windowLength));
        end = start.plusMinutes(numOfIntervals * windowLength);

        long reservationLength = ChronoUnit.SECONDS.between(start, end);
        if (TimeUnit.MINUTES.toSeconds(createDto.notificationTime()) > (reservationLength / 2))
            throw new ReservationNotificationTimeTooLongException("Notification time could not be longer than half the reservation duration.");

        /* Condition no. 1: Minimum reservation length */

        if (reservationLength < (2L * TimeUnit.MINUTES.toSeconds(windowLength)))
            throw new ReservationTooShortException("Minimum length of the reservation in eduVirt system is exactly twice as long as assumed window length.");

        /* Condition no. 2: Maximum reservation length */

        long maxRentTime = TimeUnit.HOURS.toSeconds(resourceGroupPool.getMaxRentTime());
        if (maxRentTime != 0 && reservationLength > maxRentTime)
            throw new ReservationMaxLengthExceededException("Reservation for resource group pool: %s could not be longer than: %d"
                    .formatted(resourceGroupPool.getId(), maxRentTime));

        /* Condition no. 3: Maximum number of reservations for given resource group */

        int reservationLimit = resourceGroupPool.getMaxRent();
        List<Reservation> rgTeamReservations = reservationRepository
                .findAllRgPoolReservationsForGivenTeam(resourceGroupPool, team);

        if (reservationLimit != 0 && rgTeamReservations.size() >= reservationLimit)
            throw new ResourceGroupReservationCountExceededException(
                    "Team %s has already made all available reservations for resource group pool: %s"
                            .formatted(team.getId(), resourceGroupPool.getId()));

        /* Condition no. 4: Grace period for previous reservation */

        int gracePeriodInHours = resourceGroupPool.getGracePeriod();
        List<Reservation> reservationsBefore = reservationRepository.findRgPoolReservationsForGivenTeam(
                resourceGroupPool, team, start.minusHours(gracePeriodInHours), start);

        List<Reservation> reservationsAfter = reservationRepository.findRgPoolReservationsForGivenTeam(
                resourceGroupPool, team, end, end.plusHours(gracePeriodInHours));

        if (gracePeriodInHours != 0 && !reservationsBefore.isEmpty())
            throw new ReservationGracePeriodNotFinishedException(
                    "Reservation grace period, which is %d hours, will not be finished before scheduled reservation."
                            .formatted(gracePeriodInHours));

        if (gracePeriodInHours != 0 && !reservationsAfter.isEmpty())
            throw new ReservationGracePeriodCouldNotFinishException(
                    "Reservation grace period, which is %d hours, will not be finished before next reservation."
                            .formatted(gracePeriodInHours));

        /* Condition no. 5: Maintenance intervals */

        List<MaintenanceInterval> foundIntervals = maintenanceIntervalRepository
                .findAllIntervalsInGivenTimePeriod(course.getClusterId(), start, end);

        if (!foundIntervals.isEmpty())
            throw new ReservationCreationException(I18n.RESERVATION_MAINTENANCE_INTERVAL_CONFLICT);

        /* NOTE: That's the place where actual choosing of resource group starts */

        ResourceGroup chosenResourceGroup = null;
        for (ResourceGroup resourceGroup : resourceGroupPool.getResourceGroups()) {
            /* Condition no. 6: Resources availability for given course */
            List<CourseMetric> foundCourseMetrics = courseMetricRepository.findAllByCourse(course);
            List<Reservation> foundCourseReservations = reservationRepository
                    .findCourseReservations(course, start, end);

            if (!bankerAlgorithm.process(() -> metricUtil.extractCourseMetricValues(foundCourseMetrics),
                    foundCourseReservations, resourceGroup, courseCluster, clusterHosts)) continue;

            /* Condition no. 7: Resources availability for given cluster */

            List<ClusterMetric> foundClusterMetrics = clusterMetricRepository.findAllByClusterId(course.getClusterId());
            List<Reservation> foundClusterReservations = reservationRepository
                    .findClusterReservations(course.getClusterId(), start, end);

            if (!bankerAlgorithm.process(() -> metricUtil.extractClusterMetricValues(foundClusterMetrics),
                    foundClusterReservations, resourceGroup, courseCluster, clusterHosts)) continue;

            /* Condition no. 8: Resource group availability */

            List<Reservation> foundReservations = reservationRepository
                    .findRgReservations(resourceGroup, start, end);
            if (!foundReservations.isEmpty()) continue;

            chosenResourceGroup = resourceGroup;
            break;
        }

        /* TODO: Condition check end */

        if (chosenResourceGroup == null)
            throw new ReservationCreationException("Reservation of one of the resource groups inside resource group pool %s is not possible"
                    .formatted(resourceGroupPool.getId()));

        Reservation newReservation = new Reservation(
                chosenResourceGroup, team, start, end,
                createDto.automaticStartup(),
                createDto.notificationTime()
        );

        reservationRepository.saveAndFlush(newReservation);
    }

    /* Read methods */

    @PreAuthorize("isAuthenticated()")
    @Override
    public Optional<Reservation> findReservationById(UUID reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @PreAuthorize("hasAuthority('student')")
    @Override
    public Page<Reservation> findReservationsForStatelessPod(PodStateless statelessPod, Team team, Pageable pageable) {
        return reservationRepository.findAllRgPoolReservationsForGivenTeam(
                statelessPod.getResourceGroupPool(), team, pageable);
    }

    @PreAuthorize("hasAuthority('student')")
    @Override
    public Page<Reservation> findReservationsForStatefulPod(PodStateful statefulPod, Team team, Pageable pageable) {
        return reservationRepository.findAllRgReservationsForGivenTeam(
                statefulPod.getResourceGroup(), team, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<Reservation> findRgReservations(ResourceGroup resourceGroup, LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findRgReservations(resourceGroup, start, end);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<Reservation> findRgPoolReservations(ResourceGroupPool resourceGroupPool, LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findRgPoolReservations(resourceGroupPool, start, end);
    }

    @PreAuthorize("hasAuthority('student')")
    @Override
    public List<Reservation> findRgReservationsForTeam(ResourceGroup resourceGroup, Team team, LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findRgReservationsForGivenTeam(resourceGroup, team, start, end);
    }

    @PreAuthorize("hasAuthority('student')")
    @Override
    public List<Reservation> findRgPoolReservationsForTeam(ResourceGroupPool resourceGroupPool, Team team, LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findRgPoolReservationsForGivenTeam(resourceGroupPool, team, start, end);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public Page<Reservation> findActiveReservations(UUID teamId, Pageable pageable) {
        Team foundTeam = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        return reservationRepository.findAllActiveReservations(foundTeam, currentTime, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public Page<Reservation> findHistoricalReservations(UUID teamId, Pageable pageable) {
        Team foundTeam = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        return reservationRepository.findAllHistoricalReservations(foundTeam, currentTime, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public Map<LocalDateTime, Boolean> checkResourceGroupAvailability(ResourceGroup resourceGroup, Course course,
                                                                      int windowLength, LocalDateTime start, LocalDateTime end) {
        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        Cluster cluster = clusterService.findClusterById(course.getClusterId());
        List<Host> hosts = clusterService.findAllHostsInCluster(cluster);

        List<CourseMetric> courseMetrics = courseMetricRepository.findAllByCourse(course);
        List<ClusterMetric> clusterMetrics = clusterMetricRepository.findAllByClusterId(course.getClusterId());

        LocalDateTime currentTime = start;
        while (currentTime.isBefore(end)) {
            availability.put(currentTime, establishResourceGroupAvailability(course, resourceGroup, cluster, hosts,
                    courseMetrics, clusterMetrics, currentTime, currentTime.plusMinutes(windowLength)));

            currentTime = currentTime.plusMinutes(windowLength);
        }

        boolean isOverWeek = (ChronoUnit.SECONDS.between(start, end) >= TimeUnit.DAYS.toSeconds(7)) && resourcesWarningMails;
        if (isOverWeek && checkIfWarningMailRequired(start, end, availability)) {
            List<User> addressees = userRepository.findUsersWithRole("administrator");
            addressees.addAll(course.getTeachers());

            addressees.forEach(addressee -> mailProvider.sendTemporaryCourseResourcesExhaustionEmail(
                    addressee.getFirstName(), addressee.getLastName(), addressee.getEmail(),
                    course, resourceGroup.getName(), true, start, end, addressee.getTimeZone(), addressee.getLanguage()
            ));
        }

        return availability;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public Map<LocalDateTime, Boolean> checkResourceGroupPoolAvailability(ResourceGroupPool resourceGroupPool, Course course,
                                                                          int windowLength, LocalDateTime start, LocalDateTime end) {
        Map<LocalDateTime, Boolean> availability = new HashMap<>();

        Cluster cluster = clusterService.findClusterById(course.getClusterId());
        List<Host> hosts = clusterService.findAllHostsInCluster(cluster);

        List<CourseMetric> courseMetrics = courseMetricRepository.findAllByCourse(course);
        List<ClusterMetric> clusterMetrics = clusterMetricRepository.findAllByClusterId(course.getClusterId());

        LocalDateTime currentTime = start;
        while (currentTime.isBefore(end)) {
            boolean available = false;
            for (ResourceGroup resourceGroup : resourceGroupPool.getResourceGroups()) {
                available = establishResourceGroupAvailability(course, resourceGroup, cluster, hosts,
                        courseMetrics, clusterMetrics, currentTime, currentTime.plusMinutes(windowLength));

                if (available) break;
            }

            availability.put(currentTime, available);
            currentTime = currentTime.plusMinutes(windowLength);
        }

        boolean isOverWeek = (ChronoUnit.SECONDS.between(start, end) >= TimeUnit.DAYS.toSeconds(7)) && resourcesWarningMails;
        if (isOverWeek && checkIfWarningMailRequired(start, end, availability)) {
            List<User> addressees = userRepository.findUsersWithRole("administrator");
            addressees.addAll(course.getTeachers());

            addressees.forEach(addressee -> mailProvider.sendTemporaryCourseResourcesExhaustionEmail(
                    addressee.getFirstName(), addressee.getLastName(), addressee.getEmail(),
                    course, resourceGroupPool.getName(), false, start, end, addressee.getTimeZone(), addressee.getLanguage()
            ));
        }

        return availability;
    }

    @PreAuthorize("hasAuthority('student')")
    @Override
    public int findReservationCountForStatelessPod(PodStateless statelessPod, Team team) {
        return reservationRepository.findAllRgPoolReservationsForGivenTeam(
                statelessPod.getResourceGroupPool(), team).size();
    }

    @PreAuthorize("hasAuthority('student')")
    @Override
    public int findReservationCountForStatefulPod(PodStateful statefulPod, Team team) {
        return reservationRepository.findAllRgReservationsForGivenTeam(
                statefulPod.getResourceGroup(), team).size();
    }

    @Override
    public List<Reservation> findReservationsToBegin() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime probeTimeWithTimeNeededToStop = currentTime.plusMinutes(taskTimeTolerance + vmGraceTime);
        List<Reservation> reservationList = reservationRepository.findAllReservationsToBegin(currentTime, probeTimeWithTimeNeededToStop);

        reservationList.forEach(ReservationServiceImpl::forceReservationLazyCollections);

        return reservationList;
    }

    @Override
    public List<Reservation> findReservationsToStop() {
        LocalDateTime probeTimeWithTimeNeededToStop = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime()
                .plusMinutes(taskTimeTolerance + vmGraceTime);
        List<Reservation> reservationList = reservationRepository.findAllReservationsToStop(probeTimeWithTimeNeededToStop);

        reservationList.forEach(ReservationServiceImpl::forceReservationLazyCollections);

        return reservationList;
    }

    @Override
    public List<Reservation> findReservationsToSendNotifications() {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        return reservationRepository.findAllReservationsToSendNotifications(currentTime)
                .stream()
                .filter(r -> {
                            long minBeforeReservationEnd = r.getNotificationTime() + taskTimeTolerance + vmGraceTime;
                            return !r.getEndTime().minusMinutes(minBeforeReservationEnd).isAfter(currentTime);
                        }
                )
                .toList();
    }

    private static void forceReservationLazyCollections(Reservation reservation) {
        //TODO michal: add comments XDD

        ResourceGroup resourceGroup = reservation.getResourceGroup();

        List<VirtualMachine> vms = resourceGroup.getVms();
        vms.forEach(vm -> vm.setNetworkInterfaces(new ArrayList<>(vm.getNetworkInterfaces())));

        List<ResourceGroupNetwork> networks = resourceGroup.getNetworks();
        networks.forEach(network -> network.setInterfaces(new ArrayList<>(network.getInterfaces())));

        resourceGroup.setVms(new ArrayList<>(vms));
        resourceGroup.setNetworks(new ArrayList<>(networks));

        Team team = reservation.getTeam();
        team.setUsers(new ArrayList<>(team.getUsers()));

        reservation.setResourceGroup(resourceGroup);
        reservation.setTeam(team);
    }

    /* Update / delete methods */

    @PreAuthorize("hasAuthority('student')")
    @Override
    public void finishReservationAsStudent(Reservation reservation) {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();

        if (reservation.getEndTime().isBefore(currentTime)) {
            throw new ReservationAlreadyFinishedException(
                    "Reservation %s has already finished!".formatted(reservation.getId()));
        } else if (reservation.getStartTime().isBefore(currentTime)) {
            reservation.setEndTime(currentTime);
            reservationRepository.saveAndFlush(reservation);
        } else {
            reservationRepository.delete(reservation);
        }
    }

    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    @Override
    public void finishReservationAsTeacherOrAdmin(Reservation reservation) {
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

    /* Other methods */

    @PreAuthorize("isAuthenticated()")
    private boolean checkIfWarningMailRequired(LocalDateTime start, LocalDateTime end, Map<LocalDateTime, Boolean> availability) {
        LocalDateTime startTemp = start;

        Stack<Boolean> availabilityStack = new Stack<>();
        while (startTemp.isBefore(end)) {
            if (availability.get(startTemp)) {
                availabilityStack.push(true);
                if (availabilityStack.size() >= 2) return false;
            } else availabilityStack.clear();

            startTemp = startTemp.plusMinutes(windowLength);
        }

        return true;
    }

    @PreAuthorize("isAuthenticated()")
    private boolean establishResourceGroupAvailability(Course course, ResourceGroup resourceGroup, Cluster cluster, List<Host> hosts,
                                                       List<CourseMetric> courseMetrics, List<ClusterMetric> clusterMetrics,
                                                       LocalDateTime start, LocalDateTime end) {
        List<Reservation> currentCourseReservations = reservationRepository
                .findCourseReservations(course, start, end);

        List<Reservation> currentClusterReservations = reservationRepository
                .findClusterReservations(course.getClusterId(), start, end);

        /* Fetching "effective" maintenance intervals for the cluster given course is located in
         * "Effective" -> maintenance intervals for cluster or system (which makes the cluster unavailable) */
        List<MaintenanceInterval> effectiveMaintenanceIntervals = maintenanceIntervalRepository
                .findAllIntervalsInGivenTimePeriod(course.getClusterId(), start, end);

        boolean available = bankerAlgorithm.process(() -> metricUtil.extractCourseMetricValues(courseMetrics),
                currentCourseReservations, resourceGroup, cluster, hosts) &&
                bankerAlgorithm.process(() -> metricUtil.extractClusterMetricValues(clusterMetrics),
                        currentClusterReservations, resourceGroup, cluster, hosts);

        boolean reserved = !reservationRepository.findRgReservations(resourceGroup, start, end).isEmpty();
        boolean requiredByAdmin = !effectiveMaintenanceIntervals.isEmpty();

        return available && !reserved && !requiredByAdmin;
    }
}
