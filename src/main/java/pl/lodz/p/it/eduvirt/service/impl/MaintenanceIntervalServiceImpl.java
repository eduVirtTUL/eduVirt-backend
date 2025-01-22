package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.repository.MaintenanceIntervalRepository;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.MaintenanceIntervalService;
import pl.lodz.p.it.eduvirt.util.I18n;
import pl.lodz.p.it.eduvirt.util.MailProvider;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MaintenanceIntervalServiceImpl implements MaintenanceIntervalService {

    @Value("${maintenance-interval.min-ahead}")
    private int maintenanceIntervalMinAhead;

    /* Repositories */

    private final MaintenanceIntervalRepository maintenanceIntervalRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    /* Other */

    private final MailProvider mailProvider;

    /* Create methods */

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public void createClusterMaintenanceInterval(Cluster cluster, String cause, String description, LocalDateTime beginAt, LocalDateTime endAt) {
        if (beginAt.isAfter(endAt))
            throw new MaintenanceIntervalInvalidTimeWindowException("Maintenance interval end must happen after its start");

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (beginAt.isBefore(currentTime.plusHours(maintenanceIntervalMinAhead)))
            throw new MaintenanceIntervalInvalidTimeWindowException(
                    I18n.MAINTENANCE_INTERVAL_BEGIN_TOO_EARLY);

        UUID clusterId = UUID.fromString(cluster.id());
        List<MaintenanceInterval> foundIntervals = maintenanceIntervalRepository
                .findAllIntervalsInGivenTimePeriod(beginAt, endAt, MaintenanceInterval.IntervalType.CLUSTER, clusterId);
        if (!foundIntervals.isEmpty()) throw new MaintenanceIntervalConflictException(
                "Other cluster maintenance intervals exists for cluster %s in the specified time window".formatted(clusterId));

        MaintenanceInterval maintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.CLUSTER, clusterId, beginAt, endAt);

        /* TODO: Perform logic on reservation that exist in the specified window of time
                 that is cancel all of them and send e-mail notification */

        List<Reservation> foundReservations = reservationRepository
                .findClusterReservations(clusterId, beginAt, endAt);

        handleConflictingReservations(currentTime, maintenanceInterval, foundReservations);
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public void createSystemMaintenanceInterval(String cause, String description, LocalDateTime beginAt, LocalDateTime endAt) {
        if (beginAt.isAfter(endAt))
            throw new MaintenanceIntervalInvalidTimeWindowException("Maintenance interval end must happen after its start");

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (beginAt.isBefore(currentTime.plusHours(maintenanceIntervalMinAhead)))
            throw new MaintenanceIntervalInvalidTimeWindowException(
                    I18n.MAINTENANCE_INTERVAL_BEGIN_TOO_EARLY);

        List<MaintenanceInterval> foundIntervals = maintenanceIntervalRepository
                .findAllIntervalsInGivenTimePeriod(beginAt, endAt, MaintenanceInterval.IntervalType.SYSTEM, null);
        if (!foundIntervals.isEmpty()) throw new MaintenanceIntervalConflictException(
                "Other system maintenance intervals exists for system %s in the specified time window");

        MaintenanceInterval maintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.SYSTEM, null, beginAt, endAt);

        /* TODO: Perform logic on reservation that exist in the specified window of time
                 that is cancel all of them and send e-mail notification */

        List<Reservation> foundReservations = reservationRepository
                .findSystemReservations(beginAt, endAt);

        handleConflictingReservations(currentTime, maintenanceInterval, foundReservations);
    }

    private void handleConflictingReservations(LocalDateTime currentTime, MaintenanceInterval maintenanceInterval, List<Reservation> foundReservations) {
        foundReservations.stream().filter(reservation -> reservation.getStartTime().isBefore(currentTime)).forEach(reservation -> {
            List<UUID> userIds = reservation.getTeam().getUsers().stream().map(User::getId).toList();
            /* Send e-mail notification*/
            userIds.forEach(userId -> userRepository.findById(userId).ifPresent(user -> mailProvider.sendReservationShortenedEmail(
                    user.getFirstName(), user.getLastName(), user.getEmail(), reservation, user.getTimeZone(), user.getLanguage()
            )));

            /* Save edited reservation */
            reservationRepository.saveAndFlush(reservation);
        });

        foundReservations.stream().filter(reservation -> !reservation.getStartTime().isBefore(currentTime)).forEach(reservation -> {
            List<UUID> userIds = reservation.getTeam().getUsers().stream().map(User::getId).toList();
            /* Send e-mail notification*/
            userIds.forEach(userId -> userRepository.findById(userId).ifPresent(user -> mailProvider.sendReservationRemovalEmail(
                    user.getFirstName(), user.getLastName(), user.getEmail(), reservation, user.getTimeZone(), user.getLanguage()
            )));

            /* Delete reservation */
            reservationRepository.delete(reservation);
        });

        maintenanceIntervalRepository.saveAndFlush(maintenanceInterval);
    }

    /* Read methods */

    @PreAuthorize("isAuthenticated()")
    @Override
    public Optional<MaintenanceInterval> findMaintenanceInterval(UUID intervalId) {
        return maintenanceIntervalRepository.findById(intervalId);
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public Page<MaintenanceInterval> findAllMaintenanceIntervals(UUID clusterId, boolean active, Pageable pageable) {
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (active) {
            if (clusterId != null) return maintenanceIntervalRepository.findAllActiveIntervalsForGivenCluster(clusterId, currentTime, pageable);
            return maintenanceIntervalRepository.findAllActiveIntervals(currentTime, pageable);
        }
        if (clusterId != null) return maintenanceIntervalRepository.findAllHistoricalIntervalsForGivenCluster(clusterId, currentTime, pageable);
        return maintenanceIntervalRepository.findAllHistoricalIntervals(currentTime, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<MaintenanceInterval> findAllMaintenanceIntervalsInTimePeriod(UUID clusterId, LocalDateTime start, LocalDateTime end) {
        return maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(clusterId, start, end);
    }

    /* Update / delete methods */

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public void finishMaintenanceInterval(UUID intervalId) {
        MaintenanceInterval foundInterval = maintenanceIntervalRepository.findById(intervalId)
                .orElseThrow(() -> new MaintenanceIntervalNotFound(intervalId));

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (foundInterval.getEndAt().isBefore(currentTime)) {
            throw new MaintenanceIntervalAlreadyFinishedException("Maintenance interval already finished!");
        } else if (foundInterval.getBeginAt().isBefore(currentTime)) {
            foundInterval.setEndAt(currentTime);
            maintenanceIntervalRepository.saveAndFlush(foundInterval);
        } else {
            maintenanceIntervalRepository.delete(foundInterval);
        }
    }
}
