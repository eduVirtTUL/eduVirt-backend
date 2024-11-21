package pl.lodz.p.it.eduvirt.dto.maintenance_interval;

import java.time.LocalDateTime;

public record CreateMaintenanceIntervalDto(
        String cause,
        String description,
        LocalDateTime beginAt,
        LocalDateTime endAt
) {}
