package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.MaintenanceInterval;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceIntervalService {

    void createClusterMaintenanceInterval(UUID clusterId, String cause, String description, LocalDateTime beginAt, LocalDateTime endAt);
    void createSystemMaintenanceInterval(String cause, String description, LocalDateTime beginAt, LocalDateTime endAt);

    Optional<MaintenanceInterval> findMaintenanceInterval(UUID intervalId);
    Page<MaintenanceInterval> findAllMaintenanceIntervals(UUID clusterId, boolean active, Pageable pageable);

    void cancelMaintenanceInterval(UUID intervalId);
}
