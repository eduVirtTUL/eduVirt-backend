package pl.lodz.p.it.eduvirt.dto.reservation;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReservationDto(
        @NotNull(message = "reservations.validation.null.resource.group.id")
        UUID resourceGroupId,

        @NotNull(message = "reservations.validation.null.start.time")
        LocalDateTime start,

        @NotNull(message = "reservations.validation.null.end.time")
        LocalDateTime end,

        boolean automaticStartup
) {}
