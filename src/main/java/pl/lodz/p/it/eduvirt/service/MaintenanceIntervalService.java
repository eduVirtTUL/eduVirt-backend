package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.MaintenanceInterval;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceIntervalService {

    void createClusterMaintenanceInterval(Cluster cluster, String cause, String description, LocalDateTime beginAt, LocalDateTime endAt);
    void createSystemMaintenanceInterval(String cause, String description, LocalDateTime beginAt, LocalDateTime endAt);

    Optional<MaintenanceInterval> findMaintenanceInterval(UUID intervalId);
    Page<MaintenanceInterval> findAllMaintenanceIntervals(UUID clusterId, boolean active, Pageable pageable);
    List<MaintenanceInterval> findAllMaintenanceIntervalsInTimePeriod(LocalDateTime start, LocalDateTime end);

    void finishMaintenanceInterval(UUID intervalId);
}
