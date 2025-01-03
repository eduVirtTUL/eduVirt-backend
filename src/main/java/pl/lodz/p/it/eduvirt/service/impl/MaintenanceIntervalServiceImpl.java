package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.reservation.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.exceptions.MaintenanceIntervalConflictException;
import pl.lodz.p.it.eduvirt.exceptions.MaintenanceIntervalInvalidTimeWindowException;
import pl.lodz.p.it.eduvirt.exceptions.MaintenanceIntervalNotFound;
import pl.lodz.p.it.eduvirt.repository.MaintenanceIntervalRepository;
import pl.lodz.p.it.eduvirt.service.MaintenanceIntervalService;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class MaintenanceIntervalServiceImpl implements MaintenanceIntervalService {

    private final MaintenanceIntervalRepository maintenanceIntervalRepository;

    @Override
    public void createClusterMaintenanceInterval(Cluster cluster, String cause, String description, LocalDateTime beginAt, LocalDateTime endAt) {
        if (beginAt.isAfter(endAt))
            throw new MaintenanceIntervalInvalidTimeWindowException("Maintenance interval end must happen after its start");

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (beginAt.isBefore(currentTime))
            throw new MaintenanceIntervalInvalidTimeWindowException(
                    I18n.MAINTENANCE_INTERVAL_BEGIN_AT_PAST);

        UUID clusterId = UUID.fromString(cluster.id());
        List<MaintenanceInterval> foundIntervals = maintenanceIntervalRepository
                .findAllIntervalsInGivenTimePeriod(beginAt, endAt, MaintenanceInterval.IntervalType.CLUSTER, clusterId);
        if (!foundIntervals.isEmpty()) throw new MaintenanceIntervalConflictException(
                "Other cluster maintenance intervals exists for cluster %s in the specified time window".formatted(clusterId));

        MaintenanceInterval maintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.CLUSTER, clusterId, beginAt, endAt);

        /* TODO: Perform logic on reservation that exist in the specified window of time
                 that is cancel all of them and send e-mail notification */

        maintenanceIntervalRepository.saveAndFlush(maintenanceInterval);
    }

    @Override
    public void createSystemMaintenanceInterval(String cause, String description, LocalDateTime beginAt, LocalDateTime endAt) {
        if (beginAt.isAfter(endAt))
            throw new MaintenanceIntervalInvalidTimeWindowException("Maintenance interval end must happen after its start");

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (beginAt.isBefore(currentTime))
            throw new MaintenanceIntervalInvalidTimeWindowException(
                    I18n.MAINTENANCE_INTERVAL_BEGIN_AT_PAST);

        List<MaintenanceInterval> foundIntervals = maintenanceIntervalRepository
                .findAllIntervalsInGivenTimePeriod(beginAt, endAt, MaintenanceInterval.IntervalType.SYSTEM, null);
        if (!foundIntervals.isEmpty()) throw new MaintenanceIntervalConflictException(
                "Other system maintenance intervals exists for system %s in the specified time window");

        MaintenanceInterval maintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.SYSTEM, null, beginAt, endAt);

        /* TODO: Perform logic on reservation that exist in the specified window of time
                 that is cancel all of them and send e-mail notification */

        maintenanceIntervalRepository.saveAndFlush(maintenanceInterval);
    }

    @Override
    public Optional<MaintenanceInterval> findMaintenanceInterval(UUID intervalId) {
        return maintenanceIntervalRepository.findById(intervalId);
    }

    @Override
    public Page<MaintenanceInterval> findAllMaintenanceIntervals(UUID clusterId, boolean active, Pageable pageable) {
        if (active) {
            if (clusterId != null)
                return maintenanceIntervalRepository.findAllActiveIntervalsForGivenCluster(clusterId, pageable);
            return maintenanceIntervalRepository.findAllActiveIntervals(pageable);
        }
        if (clusterId != null)
            return maintenanceIntervalRepository.findAllHistoricalIntervalsForGivenCluster(clusterId, pageable);
        return maintenanceIntervalRepository.findAllHistoricalIntervals(pageable);
    }

    @Override
    public List<MaintenanceInterval> findAllMaintenanceIntervalsInTimePeriod(UUID clusterId, LocalDateTime start, LocalDateTime end) {
        return maintenanceIntervalRepository.findAllIntervalsInGivenTimePeriod(clusterId, start, end);
    }

    @Override
    public void finishMaintenanceInterval(UUID intervalId) {
        MaintenanceInterval foundInterval = maintenanceIntervalRepository.findById(intervalId)
                .orElseThrow(() -> new MaintenanceIntervalNotFound(intervalId));

        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        if (foundInterval.getBeginAt().isBefore(currentTime)) {
            foundInterval.setEndAt(currentTime);
            maintenanceIntervalRepository.saveAndFlush(foundInterval);
        } else {
            maintenanceIntervalRepository.delete(foundInterval);
        }
    }
}
