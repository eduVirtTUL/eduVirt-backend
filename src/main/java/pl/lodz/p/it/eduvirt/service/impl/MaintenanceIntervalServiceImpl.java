package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalInvalidTimeWindowException;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalNotFound;
import pl.lodz.p.it.eduvirt.repository.eduvirt.MaintenanceIntervalRepository;
import pl.lodz.p.it.eduvirt.repository.ovirt.ClusterRepository;
import pl.lodz.p.it.eduvirt.service.MaintenanceIntervalService;
import pl.lodz.p.it.eduvirt.util.I18n;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class MaintenanceIntervalServiceImpl implements MaintenanceIntervalService {

    private final MaintenanceIntervalRepository maintenanceIntervalRepository;
    private final ClusterRepository clusterRepository;

    @Override
    public void createClusterMaintenanceInterval(UUID clusterId, String cause, String description, LocalDateTime beginAt, LocalDateTime endAt) {
        // if (!clusterRepository.existsById(clusterId)) throw new ClusterNotFoundException();

        if (beginAt.isAfter(endAt))
            throw new MaintenanceIntervalInvalidTimeWindowException();

        if (beginAt.isBefore(LocalDateTime.now()))
            throw new MaintenanceIntervalInvalidTimeWindowException(
                    I18n.MAINTENANCE_INTERVAL_BEGIN_AT_PAST);

        MaintenanceInterval maintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.CLUSTER, clusterId, beginAt, endAt);

        /* TODO: Perform logic on reservation that exist in the specified window of time
                 that is cancel all of them and send e-mail notification */

        maintenanceIntervalRepository.save(maintenanceInterval);
    }

    @Override
    public void createSystemMaintenanceInterval(String cause, String description, LocalDateTime beginAt, LocalDateTime endAt) {
        if (beginAt.isAfter(endAt))
            throw new MaintenanceIntervalInvalidTimeWindowException();

        if (beginAt.isBefore(LocalDateTime.now()))
            throw new MaintenanceIntervalInvalidTimeWindowException(
                    I18n.MAINTENANCE_INTERVAL_BEGIN_AT_PAST);

        MaintenanceInterval maintenanceInterval = new MaintenanceInterval(
                cause, description, MaintenanceInterval.IntervalType.SYSTEM, null, beginAt, endAt);

        /* TODO: Perform logic on reservation that exist in the specified window of time
                 that is cancel all of them and send e-mail notification */

        maintenanceIntervalRepository.save(maintenanceInterval);
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
    public void cancelMaintenanceInterval(UUID intervalId) {
        MaintenanceInterval foundInterval = maintenanceIntervalRepository.findById(intervalId)
                .orElseThrow(MaintenanceIntervalNotFound::new);
        maintenanceIntervalRepository.delete(foundInterval);
    }
}
