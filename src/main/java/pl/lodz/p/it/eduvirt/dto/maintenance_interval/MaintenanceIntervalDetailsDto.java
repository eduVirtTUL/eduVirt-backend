package pl.lodz.p.it.eduvirt.dto.maintenance_interval;

import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceIntervalDetailsDto(
        UUID id,
        String cause,
        String description,
        String type,
        UUID clusterId,
        LocalDateTime beginAt,
        LocalDateTime endAt
) {}
