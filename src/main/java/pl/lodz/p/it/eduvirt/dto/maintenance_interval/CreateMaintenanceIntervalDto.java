package pl.lodz.p.it.eduvirt.dto.maintenance_interval;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateMaintenanceIntervalDto(
        @NotBlank(message = "maintenanceIntervals.validation.cause.blank")
        @Size(min = 4, message = "maintenanceIntervals.validation.cause.too.short")
        @Size(max = 128, message = "maintenanceIntervals.validation.cause.too.long")
        String cause,

        @Size(max = 256, message = "maintenanceIntervals.validation.description.too.long")
        String description,

        @NotNull(message = "maintenanceIntervals.validation.null.begin.at")
        LocalDateTime beginAt,

        @NotNull(message = "maintenanceIntervals.validation.null.end.at")
        LocalDateTime endAt
) {}
